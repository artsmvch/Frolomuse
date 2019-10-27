package com.frolo.muse.ui.main.player.current

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.di.Exec
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.get.GetCurrentSongQueueUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import java.util.concurrent.Executor
import javax.inject.Inject


class CurrentSongQueueViewModel @Inject constructor(
        @Exec(Exec.Type.MAIN) private val mainThreadExecutor: Executor,
        private val player: Player,
        getCurrentSongQueueUseCase: GetCurrentSongQueueUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Song>(
        getCurrentSongQueueUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
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

    private val _isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _playingPosition: MutableLiveData<Int> = MutableLiveData()
    val playingPosition: LiveData<Int> = _playingPosition

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
        handleQueue(player.getCurrentQueue())
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
        player.swap(fromPosition, toPosition)
    }

    fun onFinishedDragging() {
        handleQueue(player.getCurrentQueue())
    }

    fun onSaveOptionSelected() {
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

}