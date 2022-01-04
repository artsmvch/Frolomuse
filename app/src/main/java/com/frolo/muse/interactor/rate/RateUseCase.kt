package com.frolo.muse.interactor.rate

import com.frolo.muse.router.AppRouter
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RateUseCase @Inject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val preferences: Preferences,
        private val appRouter: AppRouter
) {

    /**
     * Checks if it's time to ask for rating
     */
    private fun needRate(): Boolean {
        return if (preferences.rated) {
            // rated already
            false
        } else {
            // not reached specified count to show the rate dialog yet
            preferences.launchCount >= preferences.minLaunchCountForRatingRequest
        }
    }

    fun checkIfRateNeeded(): Single<Boolean> {
        return if (needRate()) {
            Completable.timer(1, TimeUnit.MINUTES)
                    .andThen(Single.just(needRate()))
                    .subscribeOn(schedulerProvider.computation())
        } else Single.just(false)
    }

    fun rate() {
        // Rated! Thanks
        preferences.rated = true
        appRouter.goToStore()
    }

    fun dismissRate() {
        // it must be a fucking PIG if he(she) doesn't want to rate my App but still using it
        val minCountForRatingRequest = preferences.launchCount * 3 // ask again after 3x additional launches
        preferences.minLaunchCountForRatingRequest = minCountForRatingRequest
    }

    fun askLater() {
        val minCountForRatingRequest = preferences.launchCount + 5 // ask again after 5 additional launches
        preferences.minLaunchCountForRatingRequest = minCountForRatingRequest
    }

    fun cancelRate() {
        val minCountForRatingRequest = preferences.launchCount + 3 // ask again after 3 additional launches
        preferences.minLaunchCountForRatingRequest = minCountForRatingRequest
    }

}