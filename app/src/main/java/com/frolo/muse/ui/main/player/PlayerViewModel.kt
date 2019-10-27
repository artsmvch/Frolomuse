package com.frolo.muse.ui.main.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.di.Exec
import com.frolo.muse.engine.*
import com.frolo.muse.interactor.media.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.DeleteMediaUseCase
import com.frolo.muse.interactor.player.ControlPlayerUseCase
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PlayerViewModel @Inject constructor(
        private val player: Player,
        @Exec(Exec.Type.MAIN) private val mainThreadExecutor: Executor,
        private val schedulerProvider: SchedulerProvider,
        private val changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        private val deleteMediaUseCase: DeleteMediaUseCase<Song>,
        private val controlPlayerUseCase: ControlPlayerUseCase,
        private val navigator: Navigator,
        private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private var playbackProgressDisposable: Disposable? = null

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onPrepared(player: Player) {
            _playbackDuration.value = player.getDuration()
            _playbackProgress.value = 0
        }
        override fun onSoughtTo(player: Player, position: Int) {
            _playbackProgress.value = position
        }
        override fun onQueueChanged(player: Player, queue: SongQueue) {
            _songQueue.value?.unregisterCallback(queueCallback)
            _songQueue.value = queue.apply {
                registerCallback(queueCallback, mainThreadExecutor)
            }
        }
        override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
            _song.value = song
            _songPosition.value = positionInQueue
            if (player.isPrepared()) {
                _playbackProgress.value = player.getProgress()
            }
            checkFavourite()
        }
        override fun onPlaybackStarted(player: Player) {
            _playbackStatus.value = true
        }
        override fun onPlaybackPaused(player: Player) {
            _playbackStatus.value = false
        }
        override fun onABChanged(player: Player, aPointed: Boolean, bPointed: Boolean) {
            _abStatus.value = Pair(aPointed, bPointed)
        }
        override fun onShuffleModeChanged(player: Player, mode: Int) {
            _shuffleMode.value = mode
        }
        override fun onRepeatModeChanged(player: Player, mode: Int) {
            _repeatMode.value = mode
        }
    }

    private val queueCallback = SongQueue.Callback { queue ->
        _invalidateSongQueueEvent.value = queue
    }

    private val _deletedSong = SingleLiveEvent<Song>()
    val deletedSong: LiveData<Song> = _deletedSong

    private val _songQueue: MutableLiveData<SongQueue> = MutableLiveData()
    val songQueue: LiveData<SongQueue> = _songQueue

    private val _invalidateSongQueueEvent: MutableLiveData<SongQueue> = SingleLiveEvent()
    val invalidateSongQueueEvent: LiveData<SongQueue> = _invalidateSongQueueEvent

    private val _song: MutableLiveData<Song> = MutableLiveData()
    val song: LiveData<Song> = _song

    val placeholderVisible: LiveData<Boolean> = Transformations.map(song) { song: Song? ->
        song == null
    }

    private val _songPosition: MutableLiveData<Int> = MediatorLiveData<Int>().apply {
        // triggers
        addSource(invalidateSongQueueEvent) {
            value = player.getCurrentPositionInQueue()
        }
    }
    val songPosition: LiveData<Int> = _songPosition

    private val _showVolumeControlEvent: MutableLiveData<Unit> = SingleLiveEvent()
    val showVolumeControlEvent: LiveData<Unit> = _showVolumeControlEvent

    private val _isFavourite = MutableLiveData<Boolean>()
    val isFavourite: LiveData<Boolean> = _isFavourite

    private val _playbackDuration: MutableLiveData<Int> = MutableLiveData()
    val playbackDuration: LiveData<Int> = _playbackDuration

    private val _playbackProgress: MutableLiveData<Int> = MutableLiveData()
    val playbackProgress: LiveData<Int> = _playbackProgress

    private val _playbackStatus: MutableLiveData<Boolean> = MutableLiveData()
    val playbackStatus: LiveData<Boolean> = _playbackStatus

    private val _abStatus: MutableLiveData<Pair<Boolean, Boolean>> = MutableLiveData()
    val abStatus: LiveData<Pair<Boolean, Boolean>> = _abStatus

    private val _shuffleMode: MutableLiveData<Int> = MutableLiveData()
    val shuffleMode: LiveData<Int> = _shuffleMode

    private val _repeatMode: MutableLiveData<Int> = MutableLiveData()
    val repeatMode: LiveData<Int> = _repeatMode

    // Confirmation
    private val _confirmDeletionEvent: MutableLiveData<Song> = SingleLiveEvent()
    val confirmDeletionEvent: LiveData<Song> = _confirmDeletionEvent

    init {
        player.registerObserver(playerObserver)
        onOpened()
    }

    private fun startObservingPlaybackProgress() {
        playbackProgressDisposable?.dispose()
        playbackProgressDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .timeInterval()
                //.takeWhile { player.isPlaying() }
                .observeOn(schedulerProvider.main())
                .subscribe { _playbackProgress.value = player.getProgress() }
    }

    private fun stopObservingPlaybackProgress() {
        playbackProgressDisposable?.dispose()
    }

    private fun checkFavourite() {
        Single.fromCallable { _song.value }
                .flatMap { changeFavouriteUseCase.getIsFavourite(it) }
                .observeOn(schedulerProvider.main())
                .subscribeFor { value ->
                    _isFavourite.value = value
                }
    }

    fun onOpened() {
        _songQueue.value = player.getCurrentQueue()
        _song.value = player.getCurrent()
        _playbackDuration.value = player.getDuration()
        _playbackProgress.value = player.getProgress()
        _playbackStatus.value = player.isPlaying()
        _abStatus.value = Pair(player.isAPointed(), player.isBPointed())
        _shuffleMode.value = player.getShuffleMode()
        _repeatMode.value = player.getRepeatMode()
        checkFavourite()
        startObservingPlaybackProgress()
    }

    fun onSongUpdate(previous: Song, updated: Song) {
        player.update(updated)
        player.getCurrent()?.let {
            if (it == previous) {
                _song.value = updated
            }
        }
    }

    fun onAlbumUpdate(previous: Album, updated: Album) {
        _songQueue.value = player.getCurrentQueue()
    }

    fun onLikeClicked() {
        Single.fromCallable { _song.value }
                .flatMap { changeFavouriteUseCase.changeFavourite(it) }
                .observeOn(schedulerProvider.main())
                .subscribeFor { value ->
                    _isFavourite.value = value
                }
    }

    fun onVolumeControlClicked() {
        _showVolumeControlEvent.value = Unit
    }

    fun onSeekProgressTo(progress: Int) {
        _playbackProgress.value = progress
    }

    fun onProgressSoughtTo(progress: Int) {
        player.seekTo(progress)
    }

    fun onSwipedToPosition(position: Int) {
        player.skipTo(position, false)
    }

    fun onPlayButtonClicked() {
        player.toggle()
    }

    fun onSkipToNextButtonClicked() {
        player.skipToNext()
    }

    fun onSkipToNextButtonLongClicked() {
        player.rewindForward(10_000)
        _playbackProgress.value = player.getProgress()
    }

    fun onSkipToPreviousButtonClicked() {
        player.skipToPrevious()
    }

    fun onSkipToPreviousButtonLongClicked() {
        player.rewindBackward(10_000)
        _playbackProgress.value = player.getProgress()
    }

    fun onShuffleModeButtonClicked() {
        player.switchToNextShuffleMode()
    }

    fun onRepeatModeButtonClicked() {
        player.switchToNextRepeatMode()
    }

    fun onABButtonClicked() {
        player.pointNextABPoint()
    }

    // Options menu

    fun onEditSongOptionSelected() {
        val song = song.value ?: return
        navigator.editSong(song)
    }

    fun onViewAlbumOptionSelected() {
        controlPlayerUseCase.getAlbum()
                .observeOn(schedulerProvider.main())
                .subscribeFor { album ->
                    navigator.openAlbum(album)
                }
    }

    fun onViewArtistOptionSelected() {
        controlPlayerUseCase.getArtist()
                .observeOn(schedulerProvider.main())
                .subscribeFor { artist ->
                    navigator.openArtist(artist)
                }
    }

    fun onViewGenreOptionSelected() {
        controlPlayerUseCase.getGenre()
                .observeOn(schedulerProvider.main())
                .subscribeFor { genre ->
                    navigator.openGenre(genre)
                }
    }

    fun onViewCurrentPlayingOptionSelected() {
        navigator.openCurrentPlaying()
    }

    fun onEditAlbumOptionSelected() {
        val song = song.value ?: return
        // actually, the view model should not create the album object
        val album = Album(song.albumId, song.album, song.artist, 1)
        navigator.editAlbum(album)
    }

    fun onShareOptionSelected() {
        val song = song.value ?: return
        val songs = listOf(song)
        navigator.shareSongs(songs)
    }

    fun onViewLyricsOptionSelected() {
        val song = song.value ?: return
        navigator.viewLyrics(song)
    }

    fun onViewPosterOptionSelected() {
        val song = song.value ?: return
        navigator.viewPoster(song)
    }

    fun onRingCutterOptionSelected() {
        val song = song.value ?: return
        navigator.openRingCutter(song)
    }

    fun onDeleteOptionSelected() {
        val song = song.value ?: return
        _confirmDeletionEvent.value = song
    }

    fun onConfirmedDeletion(song: Song) {
        deleteMediaUseCase.delete(song)
                .observeOn(schedulerProvider.main())
                .subscribeFor { _deletedSong.value = song }
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
        player.getCurrentQueue()?.unregisterCallback(queueCallback)
        stopObservingPlaybackProgress()
    }
}