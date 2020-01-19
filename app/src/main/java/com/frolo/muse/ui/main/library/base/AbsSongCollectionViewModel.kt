package com.frolo.muse.ui.main.library.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMediaUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable


abstract class AbsSongCollectionViewModel<T: Song> constructor(
        private val player: Player,
        getMediaListUseCase: GetMediaUseCase<T>,
        getMediaMenuUseCase: GetMediaMenuUseCase<T>,
        clickMediaUseCase: ClickMediaUseCase<T>,
        playMediaUseCase: PlayMediaUseCase<T>,
        shareMediaUseCase: ShareMediaUseCase<T>,
        deleteMediaUseCase: DeleteMediaUseCase<T>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<T>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<T>,
        private val schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<T>(
        getMediaListUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        getIsFavouriteUseCase,
        changeFavouriteUseCase,
        schedulerProvider,
        navigator,
        eventLogger
) {

    private var playingPositionDisposable: Disposable? = null

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
            detectPlayingPosition(mediaList.value, song)
        }
        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }
        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }
    }

    private val _isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _playingPosition: MutableLiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(mediaList) { list ->
            detectPlayingPosition(list, player.getCurrent())
        }
    }
    val playingPosition: LiveData<Int> = _playingPosition

    init {
        player.registerObserver(playerObserver)
        _isPlaying.value = player.isPlaying()
    }

    private fun detectPlayingPosition(songList: List<Song>?, song: Song?) {
        Single.fromCallable { songList?.indexOf(song) ?: -1 }
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.main())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(position: Int) {
                        _playingPosition.value = position
                    }
                    override fun onSubscribe(d: Disposable) {
                        playingPositionDisposable?.dispose()
                        playingPositionDisposable = d
                    }
                    override fun onError(e: Throwable) {
                        logError(e)
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
        playingPositionDisposable?.dispose()
    }
}