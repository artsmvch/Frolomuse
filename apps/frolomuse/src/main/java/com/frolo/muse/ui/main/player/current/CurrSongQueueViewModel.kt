package com.frolo.muse.ui.main.player.current

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.EventLiveData
import com.frolo.arch.support.map
import com.frolo.muse.common.toSongs
import com.frolo.muse.di.ExecutorQualifier
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.player.AudioSourceQueue
import com.frolo.player.SimplePlayerObserver
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetCurrentSongQueueUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max


class CurrSongQueueViewModel @Inject constructor(
    @ExecutorQualifier(ExecutorQualifier.ThreadType.MAIN)
    private val mainThreadExecutor: Executor,
    private val player: Player,
    permissionChecker: PermissionChecker,
    getCurrentSongQueueUseCase: GetCurrentSongQueueUseCase,
    getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
    clickMediaUseCase: ClickMediaUseCase<Song>,
    playMediaUseCase: PlayMediaUseCase<Song>,
    shareMediaUseCase: ShareMediaUseCase<Song>,
    deleteMediaUseCase: DeleteMediaUseCase<Song>,
    getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
    changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
    createShortcutUseCase: CreateShortcutUseCase<Song>,
    private val schedulerProvider: SchedulerProvider,
    private val appRouter: AppRouter,
    eventLogger: EventLogger
): AbsMediaCollectionViewModel<Song>(
    permissionChecker,
    getCurrentSongQueueUseCase,
    getMediaMenuUseCase,
    clickMediaUseCase,
    playMediaUseCase,
    shareMediaUseCase,
    deleteMediaUseCase,
    getIsFavouriteUseCase,
    changeFavouriteUseCase,
    createShortcutUseCase,
    schedulerProvider,
    appRouter,
    eventLogger
) {

    private var currQueue: AudioSourceQueue? = null

    private val queueCallback = AudioSourceQueue.Callback { queue ->
        handleQueue(queue)
        val positionInQueue = player.getCurrentPositionInQueue()
        _playingPosition.value = positionInQueue
        _scrollToPositionIfNotVisibleToUserEvent.setValue(getPositionToScroll(positionInQueue))
    }

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
            _playingPosition.value = positionInQueue
            _scrollToPositionIfNotVisibleToUserEvent.setValue(getPositionToScroll(positionInQueue))
        }

        override fun onPositionInQueueChanged(player: Player, positionInQueue: Int) {
            _playingPosition.value = positionInQueue
            _scrollToPositionIfNotVisibleToUserEvent.setValue(getPositionToScroll(positionInQueue))
        }

        override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
            handleQueue(queue)
        }

        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }

        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }
    }

    private val _isPlaying = MutableLiveData<Boolean>(player.isPlaying())
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _playingPosition = MutableLiveData<Int>(player.getCurrentPositionInQueue())
    val playingPosition: LiveData<Int> get() = _playingPosition

    val saveAsPlaylistOptionEnabled: LiveData<Boolean> =
        mediaList.map(false) { list: List<*>? -> !list.isNullOrEmpty() }

    /**
     * This is a flag that indicates whether the scroll-to-position prompt was shown or not.
     * This flag resets to false every time [onStart] is called.
     * The prompt to scroll to play position may be shown only if this flag is false.
     */
    private var scrollToPositionPromptShown: Boolean = false

    private val _scrollToPositionButtonVisible = MutableLiveData<Boolean>(false)

    private val _scrollToPositionIfNotVisibleToUserEvent = EventLiveData<Int>()
    val scrollToPositionIfNotVisibleToUserEvent: LiveData<Int>
        get() = _scrollToPositionIfNotVisibleToUserEvent

    private var hideScrollToPositionButtonDisposable: Disposable? = null

    private var mapQueueDisposable: Disposable? = null

    init {
        player.registerObserver(playerObserver)
    }

    // TODO: this method should not be here, the view model only should use GetCurrentSongQueueUseCase as data source
    private fun handleQueue(queue: AudioSourceQueue?) {
        if (currQueue !== queue) {
            currQueue?.unregisterCallback(queueCallback)
            currQueue = queue
            queue?.registerCallback(queueCallback, mainThreadExecutor)
        }
        Single.fromCallable { queue?.snapshot?.toSongs().orEmpty() }
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.main())
            .doOnSubscribe {
                mapQueueDisposable?.dispose()
                mapQueueDisposable = it
            }
            .subscribeFor { list ->
                submitMediaList(list)
                _scrollToPositionIfNotVisibleToUserEvent.setValue(
                    getPositionToScroll(player.getCurrentPositionInQueue())
                )
            }
    }

    override fun onStart() {
        super.onStart()
        scrollToPositionPromptShown = false
        handleQueue(player.getCurrentQueue())
    }

    /**
     * Called when a chunk of the song list is shown in View.
     * The shown chunk represents items in range [fromPosition]..[toPosition].
     * If the current play position is not in this range,
     * then we can prompt the user to scroll to this position.
     *
     * The scroll button will be hidden in [SCROLL_BUTTON_VISIBLE_TIMEOUT],
     * so the user will have some time to decide whether he wants or not to scroll to the play position.
     */
    fun onListChunkShown(fromPosition: Int, toPosition: Int) {
        if (scrollToPositionPromptShown) {
            // It was shown before, we don't want to prompt the user again
            return
        }

        val playingPosition = playingPosition.value ?: return
        if (playingPosition !in fromPosition..toPosition) {
            scrollToPositionPromptShown = true
            _scrollToPositionButtonVisible.value = true

            Completable.timer(SCROLL_BUTTON_VISIBLE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .observeOn(schedulerProvider.main())
                    .doOnSubscribe {
                        hideScrollToPositionButtonDisposable?.dispose()
                        hideScrollToPositionButtonDisposable = it
                    }
                    .subscribeFor {
                        _scrollToPositionButtonVisible.value = false
                    }
        }
    }

    fun onScrolled() {
        hideScrollToPositionButtonDisposable?.dispose()
        hideScrollToPositionButtonDisposable = null
        _scrollToPositionButtonVisible.value = false
    }

    fun onItemPositionClicked(item: Song, position: Int) {
        onItemClicked(item)
        if (isInContextualMode.value != true) {
            if (player.getCurrentPositionInQueue() == position) {
                player.toggle()
            } else {
                player.skipTo(position, true)
            }
        }
    }

    fun onItemPositionDismissed(item: Song, position: Int) {
        player.removeAt(position)
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        player.moveItem(fromPosition, toPosition)
    }

    fun onSaveAsPlaylistOptionSelected() {
        val songs = ArrayList(mediaList.value ?: emptyList())
        appRouter.createPlaylist(songs)
    }

    override fun handleItemClick(item: Song) {
    }

    override fun onCleared() {
        super.onCleared()
        player.unregisterObserver(playerObserver)
        player.getCurrentQueue()?.unregisterCallback(queueCallback)
    }

    companion object {
        private const val SCROLL_BUTTON_VISIBLE_TIMEOUT = 3_000L

        /**
         * Determines the position in the list to scroll according to the current playing [positionInQueue].
         */
        fun getPositionToScroll(positionInQueue: Int): Int = max(0, positionInQueue - 2)
    }

}