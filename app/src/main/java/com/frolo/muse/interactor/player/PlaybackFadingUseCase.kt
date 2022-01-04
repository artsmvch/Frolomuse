package com.frolo.muse.interactor.player

import com.frolo.player.PlaybackFadingStrategy
import com.frolo.player.Player
import com.frolo.muse.model.FloatRange
import com.frolo.muse.model.playback.PlaybackFadingParams
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class PlaybackFadingUseCase @Inject constructor(
    private val preferences: Preferences,
    private val player: Player,
    private val schedulerProvider: SchedulerProvider
) {

    fun getPlaybackFadingDurationRange(): Single<FloatRange> = Single.just(FloatRange.of(0f, 30f))

    fun getCurrentPlaybackFadingParams(): Flowable<PlaybackFadingParams> =
            preferences.playbackFadingParams.subscribeOn(schedulerProvider.worker())

    fun applyPlaybackFadingDuration(duration: Int): Completable {
        return Completable.fromAction {
            val strategy = PlaybackFadingStrategy.withSmartStaticInterval(duration)
            player.setPlaybackFadingStrategy(strategy)
        }
    }

    fun applyAndSavePlaybackFadingDuration(duration: Int): Completable {
        return applyPlaybackFadingDuration(duration)
            .andThen(Completable.defer {
                val params = PlaybackFadingParams.create(duration, true)
                preferences.setPlaybackFadingParams(params).subscribeOn(schedulerProvider.worker())
            })
    }

}