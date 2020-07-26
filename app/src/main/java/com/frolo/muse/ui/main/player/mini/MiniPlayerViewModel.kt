package com.frolo.muse.ui.main.player.mini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.map
import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MiniPlayerViewModel @Inject constructor(
    private val player: Player,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _currentSong = MutableLiveData<Song>(player.getCurrent()?.toSong())
    val currentSong: LiveData<Song> get() = _currentSong

    val playerControllersEnabled: LiveData<Boolean> =
        currentSong.map(false) { song: Song? -> song != null }

    private val _isPlaying = MutableLiveData<Boolean>(player.isPlaying())
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _maxProgress = MutableLiveData<Int>(player.getDuration())
    val maxProgress: LiveData<Int> get() = _maxProgress

    private val _progress = MutableLiveData<Int>(player.getProgress())
    val progress: LiveData<Int> get() = _progress

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            _currentSong.value = item?.toSong()
        }

        override fun onPrepared(player: Player) {
            _maxProgress.value = player.getDuration()
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

        // For observing player's progress
        Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(schedulerProvider.main())
            .subscribeFor {
                _progress.value = player.getProgress()
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