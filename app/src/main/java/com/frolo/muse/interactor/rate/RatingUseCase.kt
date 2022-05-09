package com.frolo.muse.interactor.rate

import com.frolo.muse.repository.Preferences
import com.frolo.muse.router.AppRouter
import com.frolo.muse.repository.RatingPreferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RatingUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val preferences: Preferences,
    private val ratingPreferences: RatingPreferences,
    private val appRouter: AppRouter
) {

    fun isRatingRequested(): Single<Boolean> {
        return ratingPreferences.isRated()
            .first(false)
            .flatMap { isRated ->
                if (isRated) {
                    Single.just(false)
                } else {
                    ratingPreferences.getMinLaunchCountForRatingRequest()
                        .firstOrError()
                        .map { count ->
                            preferences.launchCount >= count
                        }
                        .delay(1, TimeUnit.MINUTES)
                }
            }
    }

    fun positiveAnswer(): Completable {
        // Rated! Thanks
        return ratingPreferences.setRated(true)
            .observeOn(schedulerProvider.main())
            .doOnComplete { appRouter.goToStore() }
    }

    fun negativeAnswer(): Completable {
        // Ask again after 3x launches
        val newCount = preferences.launchCount * 3
        return ratingPreferences.setMinLaunchCountForRatingRequest(newCount)
    }

    fun neutralAnswer(): Completable {
        // Ask again after 5 additional launches
        val newCount = preferences.launchCount + 5
        return ratingPreferences.setMinLaunchCountForRatingRequest(newCount)
    }

    fun cancel(): Completable {
        // Ask again after 3 additional launches
        val newCount = preferences.launchCount + 3
        return ratingPreferences.setMinLaunchCountForRatingRequest(newCount)
    }

}