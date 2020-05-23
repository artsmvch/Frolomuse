package com.frolo.muse.interactor.rate

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RateUseCase @Inject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val preferences: Preferences,
        private val navigator: Navigator
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
            preferences.openCount >= preferences.openCountToRate
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
        navigator.goToStore()
    }

    fun dismissRate() {
        // it must be a fucking PIG if he(she) doesn't want to rate my App but still using it
        val countToRate = preferences.openCount * 3 // ask again after 3x additional launches
        preferences.openCountToRate = countToRate
    }

    fun askLater() {
        val nextCount = preferences.openCount + 5 // ask again after 5 additional launches
        preferences.openCountToRate = nextCount
    }

    fun cancelRate() {
        val countBeforeRating = preferences.openCount + 3 // ask again after 3 additional launches
        preferences.openCountToRate = countBeforeRating
    }

}