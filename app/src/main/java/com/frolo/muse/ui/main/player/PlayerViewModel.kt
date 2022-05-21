package com.frolo.muse.ui.main.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.combine
import com.frolo.muse.arch.liveDataOf
import com.frolo.muse.arch.map
import com.frolo.muse.common.*
import com.frolo.muse.di.ExecutorQualifier
import com.frolo.muse.interactor.feature.FeaturesUseCase
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.DeleteMediaUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.player.ControlPlayerUseCase
import com.frolo.muse.interactor.player.ResolveSoundWaveUseCase
import com.frolo.muse.router.AppRouter
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logPlayerOptionsMenuShown
import com.frolo.muse.model.ABState
import com.frolo.muse.model.event.DeletionConfirmation
import com.frolo.muse.model.event.DeletionType
import com.frolo.music.model.Album
import com.frolo.music.model.Media
import com.frolo.music.model.Song
import com.frolo.muse.model.sound.SoundWave
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.rx.flowable.doOnNextIndexed
import com.frolo.muse.ui.base.BaseViewModel
import com.frolo.player.AudioSource
import com.frolo.player.AudioSourceQueue
import com.frolo.player.Player
import com.frolo.player.SimplePlayerObserver
import java.util.concurrent.Executor
import javax.inject.Inject


class PlayerViewModel @Inject constructor(
    private val player: Player,
    @ExecutorQualifier(ExecutorQualifier.Type.MAIN) private val mainThreadExecutor: Executor,
    private val schedulerProvider: SchedulerProvider,
    private val getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
    private val changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
    private val deleteMediaUseCase: DeleteMediaUseCase<Song>,
    private val controlPlayerUseCase: ControlPlayerUseCase,
    private val resolveSoundWaveUseCase: ResolveSoundWaveUseCase,
    private val featuresUseCase: FeaturesUseCase,
    private val appRouter: AppRouter,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onPrepared(player: Player, duration: Int, progress: Int) {
            _playbackDuration.value = duration
            _playbackProgress.value = progress
        }

        override fun onSoughtTo(player: Player, position: Int) {
            _playbackProgress.value = position
        }

        override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
            handleQueue(queue)
        }

        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            _song.value = item?.toSong()
            _currPosition.value = positionInQueue
            _playbackProgress.value = 0
            startObservingPlaybackProgress(item)
        }

        override fun onAudioSourceUpdated(player: Player, item: AudioSource) {
            _song.value = item.toSong()
        }

        override fun onPositionInQueueChanged(player: Player, positionInQueue: Int) {
            _currPosition.value = positionInQueue
        }

        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }

        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }

        override fun onABChanged(player: Player, aPointed: Boolean, bPointed: Boolean) {
            _abState.value = ABState(aPointed, bPointed)
        }

        override fun onShuffleModeChanged(player: Player, mode: Int) {
            _shuffleMode.value = mode
        }

        override fun onRepeatModeChanged(player: Player, mode: Int) {
            _repeatMode.value = mode
        }
    }

    private val queueCallback = AudioSourceQueue.Callback { queue ->
        _invalidateAudioSourceQueueEvent.value = queue
        _currPosition.value = player.getCurrentPositionInQueue()
    }

    private val _songDeletedEvent = SingleLiveEvent<Song>()
    val songDeletedEvent: LiveData<Song> get() = _songDeletedEvent

    private val _audioSourceQueue = MutableLiveData<AudioSourceQueue>()

    val audioSourceList: LiveData<List<AudioSource>> =
        Transformations.map(_audioSourceQueue) { queue -> queue.snapshot }

    private val _invalidateAudioSourceQueueEvent = SingleLiveEvent<AudioSourceQueue>()
    val invalidateAudioSourceListEvent: LiveData<List<AudioSource>> =
        Transformations.map(_invalidateAudioSourceQueueEvent) { queue -> queue.snapshot }

    private val _song = MutableLiveData<Song>(null)
    val song: LiveData<Song> get() = _song

    val albumArtCarouselVisible: LiveData<Boolean> =
        song.map(false) { song: Song? -> song != null }

    val playerControllersEnabled: LiveData<Boolean> =
        song.map(false) { song: Song? -> song != null }

    val soundWave: LiveData<SoundWave> =
        Transformations.switchMap(song) { song ->
            val source: String? = song?.source

            if (source == null) liveDataOf<SoundWave>(null)
            else MutableLiveData<SoundWave>().apply {
                resolveSoundWaveUseCase.resolveSoundWave(source)
                    .observeOn(schedulerProvider.main())
                    .doOnError { value = null }
                    .subscribeFor(key = "resolve_sound_wave") { sound ->
                        value = sound
                    }
            }
        }

    private val _currPosition = MutableLiveData<Int>(player.getCurrentPositionInQueue())
    val currPosition: LiveData<Int> = Transformations.distinctUntilChanged(_currPosition)

    private val _showVolumeControlEvent = SingleLiveEvent<Unit>()
    val showVolumeControlEvent: LiveData<Unit>
        get() = _showVolumeControlEvent

    private val _animateFavouriteEvent = SingleLiveEvent<Boolean>()
    val animateFavouriteEvent: LiveData<Boolean> get() = _animateFavouriteEvent

    val isFavourite: LiveData<Boolean> =
        Transformations.switchMap(song) { song: Song? ->
            if (song != null) {
                MutableLiveData<Boolean>().apply {
                    getIsFavouriteUseCase.isFavourite(song)
                        .onErrorReturnItem(false)
                        .observeOn(schedulerProvider.main())
                        .doOnNextIndexed { index, value ->
                            if (index >= 1) {
                                // If the index is greater than or equal to 1, it means that
                                // the change of the favourite status was triggered by the user,
                                // and we can animate this change.
                                _animateFavouriteEvent.value = value
                            }
                        }
                        .subscribeFor(key = "is_favourite") {
                            value = it
                        }
                }
            } else liveDataOf(false)
        }

    private val _playbackDuration = MutableLiveData<Int>()
    val playbackDuration: LiveData<Int> get() = _playbackDuration

    private val _playbackProgress = MutableLiveData<Int>()
    val playbackProgress: LiveData<Int> get() = _playbackProgress

    val progressPercent: LiveData<Float> =
        combine(playbackDuration, playbackProgress) { duration, progress ->
            if (duration != null && progress != null)
                progress.toFloat() / duration
            else 0.0f
        }

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _abState = MutableLiveData<ABState>()
    val abState: LiveData<ABState> get() = _abState

    private val _shuffleMode = MutableLiveData<Int>()
    val shuffleMode: LiveData<Int> get() = _shuffleMode

    private val _repeatMode = MutableLiveData<Int>()
    val repeatMode: LiveData<Int> get() = _repeatMode

    // Confirmation
    private val _confirmDeletionEvent = SingleLiveEvent<DeletionConfirmation<Song>>()
    val confirmDeletionEvent: LiveData<DeletionConfirmation<Song>> get() = _confirmDeletionEvent

    // Menu options
    private val _showMenuOptionsEvent = SingleLiveEvent<PlayerOptionsMenu>()
    val showOptionsMenuEvent: LiveData<PlayerOptionsMenu> get() = _showMenuOptionsEvent

    init {
        player.registerObserver(playerObserver)
    }

    private fun handleQueue(queue: AudioSourceQueue?) {
        val currQueueValue = _audioSourceQueue.value
        if (currQueueValue !== queue) {
            currQueueValue?.unregisterCallback(queueCallback)
            _audioSourceQueue.value = queue?.apply {
                registerCallback(queueCallback, mainThreadExecutor)
            }
        }
    }

    private fun startObservingPlaybackProgress(audioSource: AudioSource?) {
        PlayerProgressObserver.spawn(player, audioSource)
            .observeOn(schedulerProvider.main())
            .subscribeFor(key = "observing_playback_progress") { progress ->
                _playbackProgress.value = progress
            }
    }

    /**
     * Should be called when the user interface has been created and the view model has been observed.
     * From this point, the state of the view model is synced with the state of the player and its queue.
     */
    fun onUiCreated() {
        handleQueue(player.getCurrentQueue())
        val currentAudioSource: AudioSource? = player.getCurrent()
        _song.value = currentAudioSource?.toSong()
        _playbackDuration.value = player.getDuration()
        _playbackProgress.value = player.getProgress()
        _isPlaying.value = player.isPlaying()
        _abState.value = ABState(player.isAPointed(), player.isBPointed())
        _shuffleMode.value = player.getShuffleMode()
        _repeatMode.value = player.getRepeatMode()
        startObservingPlaybackProgress(currentAudioSource)
    }

    fun onLikeClicked() {
        song.value?.also { safeValue ->
            changeFavouriteUseCase.changeFavourite(safeValue)
                .observeOn(schedulerProvider.main())
                .subscribeFor {  }
        }
    }

    fun onVolumeControlClicked() {
        _showVolumeControlEvent.value = Unit
    }

    fun onSeekProgressToPercent(percent: Float) {
        _playbackProgress.value = getProgressFromPercent(percent)
    }

    fun onProgressSoughtToPercent(percent: Float) {
        player.seekTo(getProgressFromPercent(percent))
    }

    private fun getProgressFromPercent(percent: Float): Int {
        val duration = playbackDuration.value ?: 0
        return (duration * percent).toInt()
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
        player.rewindForward()
        _playbackProgress.value = player.getProgress()
    }

    fun onSkipToPreviousButtonClicked() {
        player.skipToPrevious()
    }

    fun onSkipToPreviousButtonLongClicked() {
        player.rewindBackward()
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
        appRouter.editSong(song)
    }

    fun onViewAlbumOptionSelected() {
        controlPlayerUseCase.getAlbum()
            .observeOn(schedulerProvider.main())
            .subscribeFor { album ->
                appRouter.openAlbum(album)
            }
    }

    fun onViewArtistOptionSelected() {
        controlPlayerUseCase.getArtist()
            .observeOn(schedulerProvider.main())
            .subscribeFor { artist ->
                appRouter.openArtist(artist)
            }
    }

    fun onViewGenreOptionSelected() {
        controlPlayerUseCase.getGenre()
            .observeOn(schedulerProvider.main())
            .subscribeFor { genre ->
                appRouter.openGenre(genre)
            }
    }

    fun onViewCurrentPlayingOptionSelected() {
        appRouter.openCurrentPlaying()
    }

    fun onEditAlbumOptionSelected() {
        val song = song.value ?: return
        // actually, the view model should not create the album object
        val album = Album(song.albumId, song.album, song.artist, 1)
        appRouter.editAlbum(album)
    }

    fun onShareOptionSelected() {
        val song = song.value ?: return
        val songs = listOf(song)
        appRouter.shareSongs(songs)
    }

    fun onAddToPlaylistOptionSelected() {
        val song = song.value ?: return
        val items = arrayListOf<Media>(song)
        appRouter.addMediaItemsToPlaylist(items)
    }

    fun onViewLyricsOptionSelected() {
        val song = song.value ?: return
        appRouter.viewLyrics(song)
    }

    fun onViewPosterOptionSelected() {
        val song = song.value ?: return
        appRouter.viewPoster(song)
    }

    fun onRingCutterOptionSelected() {
        val song = song.value ?: return
        appRouter.openRingCutter(song)
    }

    fun onDeleteOptionSelected() {
        val song = song.value ?: return
        _confirmDeletionEvent.value = DeletionConfirmation(song, null)
    }

    fun onConfirmedDeletion(song: Song, type: DeletionType) {
        deleteMediaUseCase.delete(song, type)
            .observeOn(schedulerProvider.main())
            .subscribeFor { _songDeletedEvent.value = song }
    }

    fun onOptionsMenuClicked() {
        featuresUseCase.isLyricsViewerEnabled()
            .map { isEnabled ->
                PlayerOptionsMenu(
                    isLyricsViewerEnabled = isEnabled
                )
            }
            .onErrorReturnItem(PlayerOptionsMenu())
            .observeOn(schedulerProvider.main())
            .doOnSuccess { eventLogger.logPlayerOptionsMenuShown() }
            .subscribeFor(key = "check_features_for_options_menu") { menuOptions ->
                _showMenuOptionsEvent.value = menuOptions
            }
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
        player.getCurrentQueue()?.unregisterCallback(queueCallback)
    }
}