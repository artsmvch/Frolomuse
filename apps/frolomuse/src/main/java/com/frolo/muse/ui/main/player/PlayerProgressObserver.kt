package com.frolo.muse.ui.main.player

import com.frolo.muse.common.duration
import com.frolo.player.AudioSource
import com.frolo.player.Player
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


internal object PlayerProgressObserver {

    fun spawn(player: Player, audioSource: AudioSource? = null): Flowable<Int> {
        val periodInMs: Long = if (audioSource != null) {
            // Min 20 ms, max 1000 ms
            (audioSource.duration / 100L).coerceIn(20L, 1000L)
        } else {
            1000L
        }
        return Flowable.interval(0L, periodInMs, TimeUnit.MILLISECONDS)
            .timeInterval()
            .onBackpressureLatest()
            .observeOn(Schedulers.computation())
            .map { player.getProgress() }
    }

}