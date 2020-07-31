package com.frolo.muse.engine.service.observers

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.repository.Preferences
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Observes the state of the player state and saves its changes to the current queue,
 * current item, shuffle and repeat modes in [preferences],
 * thus, the state can be restored for the player later.
 */
class PlayerStateSaver constructor(
    private val preferences: Preferences
): SimplePlayerObserver() {

    private var positionObserverDisposable: Disposable? = null

    override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
        saveLastQueue(queue)
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        saveLastAudioSource(item)
    }

    override fun onPlaybackStarted(player: Player) {
        positionObserverDisposable?.dispose()
        positionObserverDisposable = Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
                .timeInterval()
                .subscribe { saveLastPlaybackPosition(player) }
    }

    override fun onPlaybackPaused(player: Player) {
        positionObserverDisposable?.dispose()
    }

    override fun onSoughtTo(player: Player, position: Int) {
        saveLastPlaybackPosition(player)
    }

    override fun onRepeatModeChanged(player: Player, mode: Int) {
        saveRepeatMode(player)
    }

    override fun onShuffleModeChanged(player: Player, mode: Int) {
        saveShuffleMode(player)
    }

    override fun onShutdown(player: Player) {
        positionObserverDisposable?.dispose()
    }

    private fun saveLastQueue(queue: AudioSourceQueue?) {
        if (queue != null) {
            preferences.apply {
                saveLastMediaCollectionType(queue.type)
                saveLastMediaCollectionId(queue.id)
            }
        }
    }

    private fun saveLastAudioSource(item: AudioSource?) {
        if (item != null) {
            preferences.saveLastSongId(item.id)
        }
    }

    private fun saveLastPlaybackPosition(player: Player) {
        val position = player.getProgress()
        preferences.saveLastPlaybackPosition(position)
    }

    private fun saveRepeatMode(player: Player) {
        preferences.saveRepeatMode(player.getRepeatMode())
    }

    private fun saveShuffleMode(player: Player) {
        preferences.saveShuffleMode(player.getShuffleMode())
    }

}