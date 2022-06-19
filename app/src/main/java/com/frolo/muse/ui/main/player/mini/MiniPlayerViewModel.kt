package com.frolo.muse.ui.main.player.mini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.distinctUntilChanged
import com.frolo.arch.support.map
import com.frolo.muse.common.toSong
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.player.SimplePlayerObserver
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import com.frolo.muse.ui.main.player.PlayerProgressObserver
import javax.inject.Inject


class MiniPlayerViewModel @Inject constructor(
    private val player: Player,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> get() = _currentSong

    val playerControllersEnabled: LiveData<Boolean> =
        currentSong.map(false) { song: Song? -> song != null }

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _maxProgress = MutableLiveData<Int>()
    val maxProgress: LiveData<Int> get() = _maxProgress

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int> get() = _progress.distinctUntilChanged()

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            _currentSong.value = item?.toSong()
            startObservingPlaybackProgress(item)
        }

        override fun onAudioSourceUpdated(player: Player, item: AudioSource) {
            _currentSong.value = item.toSong()
        }

        override fun onPrepared(player: Player, duration: Int, progress: Int) {
            _maxProgress.value = duration
        }

        override fun onSoughtTo(player: Player, position: Int) {
            _progress.value = position
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

    fun onUiCreated() {
        _currentSong.value = player.getCurrent()?.toSong()
        _isPlaying.value = player.isPlaying()
        _maxProgress.value = player.getDuration()
        _progress.value = player.getProgress()
        startObservingPlaybackProgress(player.getCurrent())
    }

    private fun startObservingPlaybackProgress(audioSource: AudioSource?) {
        PlayerProgressObserver.spawn(player, audioSource)
            .observeOn(schedulerProvider.main())
            .subscribeFor(key = "observing_playback_progress") { progress ->
                _progress.value = progress
            }
    }

    fun onPlayButtonClicked() {
        player.toggle()
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
    }

}