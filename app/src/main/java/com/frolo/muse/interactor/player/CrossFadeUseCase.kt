package com.frolo.muse.interactor.player

import com.frolo.muse.engine.CrossFadeStrategy
import com.frolo.muse.engine.Player
import com.frolo.muse.model.FloatRange
import com.frolo.muse.model.crossfade.CrossFadeParams
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class CrossFadeUseCase @Inject constructor(
    private val preferences: Preferences,
    private val player: Player,
    private val schedulerProvider: SchedulerProvider
) {

    fun getCrossFadeRange(): Single<FloatRange> = Single.just(FloatRange.of(0f, 30f))

    fun getCurrentCrossFadeParams(): Flowable<CrossFadeParams> = preferences.crossFadeParams

    fun applyCrossFadeDuration(duration: Int): Completable {
        return Completable.fromAction {
            val strategy = CrossFadeStrategy.withSmartStaticInterval(duration)
            player.setCrossFadeStrategy(strategy)
        }
    }

    fun applyAndSaveCrossFadeDuration(duration: Int): Completable {
        return applyCrossFadeDuration(duration)
            .andThen(Completable.defer {
                val params = CrossFadeParams.create(duration, true)
                preferences.setCrossFadeParams(params)
            })
    }

}