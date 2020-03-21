package com.frolo.muse.ui.main.player.mini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class MiniPlayerViewModel @Inject constructor(
    private val player: Player,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _currentSong = MutableLiveData<Song>(player.getCurrent())
    val currentSong: LiveData<Song> get() = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(player.isPlaying())
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
            _currentSong.value = song
        }

        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }

        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }
    }

    init {
        player.registerObserver(playerObserver)
    }

    fun onPreviousClicked() {
        player.skipToPrevious()
    }

    fun onNextClicked() {
        player.skipToNext()
    }

    fun onPlayButtonClicked() {
        player.toggle()
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
    }

}