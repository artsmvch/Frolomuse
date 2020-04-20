package com.frolo.muse.ui.main.player.current

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.di.Exec
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetCurrentSongQueueUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CurrSongQueueViewModel @Inject constructor(
        @Exec(Exec.Type.MAIN) private val mainThreadExecutor: Executor,
        private val player: Player,
        getCurrentSongQueueUseCase: GetCurrentSongQueueUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        private val schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Song>(
        getCurrentSongQueueUseCase,
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

    private var currSongQueue: SongQueue? = null

    private val queueCallback = SongQueue.Callback { queue ->
        submitMediaList(queue.makeList())
        _playingPosition.value = player.getCurrentPositionInQueue()
    }

    private val playerObserver = object : SimplePlayerObserver() {
        override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
            _playingPosition.value = positionInQueue
        }
        override fun onQueueChanged(player: Player, queue: SongQueue) {
            handleQueue(queue)
        }
        override fun onPlaybackStarted(player: Player) {
            _isPlaying.value = true
        }
        override fun onPlaybackPaused(player: Player) {
            _isPlaying.value = false
        }
    }

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _playingPosition = MutableLiveData<Int>()
    val playingPosition: LiveData<Int> get() = _playingPosition

    /**
     * This is a flag that indicates whether the scroll-to-position prompt was shown or not.
     * This flag resets to false every time [onStart] is called.
     * The prompt to scroll to play position may be shown only if this flag is false.
     */
    private var scrollToPositionPromptShown: Boolean = false

    private val _scrollToPositionButtonVisible = MutableLiveData<Boolean>(false)
    val scrollToPositionButtonVisible: LiveData<Boolean> get() = _scrollToPositionButtonVisible

    private val _scrollToPositionEvent = SingleLiveEvent<Int>()
    val scrollToPositionEvent: LiveData<Int> get() = _scrollToPositionEvent

    private var hideScrollToPositionButtonDisposable: Disposable? = null

    init {
        player.registerObserver(playerObserver)
        _isPlaying.value = player.isPlaying()
        _playingPosition.value = player.getCurrentPositionInQueue()
    }

    private fun handleQueue(queue: SongQueue?) {
        currSongQueue?.unregisterCallback(queueCallback)
        currSongQueue = queue
        queue?.registerCallback(queueCallback, mainThreadExecutor)
        submitMediaList(queue?.makeList() ?: emptyList())
    }

    fun onStart() {
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

    fun onScrollToPositionClicked() {
        hideScrollToPositionButtonDisposable?.dispose()
        hideScrollToPositionButtonDisposable = null
        _scrollToPositionButtonVisible.value = false

        val targetPosition = playingPosition.value ?: return
        _scrollToPositionEvent.value = targetPosition
    }

    fun onStop() {
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
        player.remove(position)
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        player.moveItem(fromPosition, toPosition)
    }

    fun onSaveAsPlaylistOptionSelected() {
        val songs = ArrayList(mediaList.value ?: emptyList())
        navigator.createPlaylist(songs)
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
    }

}