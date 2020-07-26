package com.frolo.muse.engine.service

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.repository.Preferences
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class PlayerStateObserver constructor(
    private val preferences: Preferences
): SimplePlayerObserver() {

    private var positionObserverDisposable: Disposable? = null

    override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
        saveLastSongQueue(player)
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        saveLastSong(player)
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

    private fun saveLastSongQueue(player: Player) {
        // Save it every time a queue attached
        player.getCurrentQueue()?.let { queue ->
            preferences.apply {
                saveLastMediaCollectionType(queue.type)
                saveLastMediaCollectionId(queue.id)
            }
        }
    }

    private fun saveLastSong(player: Player) {
        // Save it every time a song changed
        player.getCurrent()?.let { song ->
            preferences.saveLastSongId(song.id)
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