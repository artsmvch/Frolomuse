package com.frolo.muse.ui.main.library.playlists.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetPlaylistUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logShortcutCreated
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import com.frolo.muse.ui.main.library.base.AssociatedWithMediaItem
import io.reactivex.Single


class PlaylistViewModel constructor(
        player: Player,
        permissionChecker: PermissionChecker,
        private val getPlaylistUseCase: GetPlaylistUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        private val playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        createSongShortcutUseCase: CreateShortcutUseCase<Song>,
        private val createPlaylistShortcutUseCase: CreateShortcutUseCase<Playlist>,
        private val schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        private val eventLogger: EventLogger
): AbsSongCollectionViewModel<Song>(
        player,
        permissionChecker,
        getPlaylistUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        getIsFavouriteUseCase,
        changeFavouriteUseCase,
        createSongShortcutUseCase,
        schedulerProvider,
        navigator,
        eventLogger
), AssociatedWithMediaItem {

    private val _playlist: MutableLiveData<Playlist> by lazy {
        MutableLiveData<Playlist>().apply {
            getPlaylistUseCase.getPlaylist()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }
    val playlist: LiveData<Playlist> get() = _playlist

    private val _isSwappingEnabled: MutableLiveData<Boolean> by lazy {
        MediatorLiveData<Boolean>().apply {
            value = false

            getPlaylistUseCase
                .isCurrentSortOrderSwappable()
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                    value = it
                }
        }
    }
    val isSwappingEnabled: LiveData<Boolean> get() = _isSwappingEnabled

    private val _confirmPlaylistShortcutCreationEvent = SingleLiveEvent<Playlist>()
    val confirmPlaylistShortcutCreationEvent: LiveData<Playlist>
        get() = _confirmPlaylistShortcutCreationEvent

    override val associatedMediaItem: Media? get() = playlist.value

    fun onEditPlaylistOptionSelected() {
        getPlaylistUseCase.edit(playlist.value)
    }

    fun onAddSongButtonClicked() {
        getPlaylistUseCase.addSongs()
    }

    fun onPlayButtonClicked() {
        val snapshot = mediaList.value.orEmpty()
        playMediaUseCase.play(snapshot, associatedMediaItem).subscribeFor {  }
    }

    fun onItemRemoved(item: Song) {
        getPlaylistUseCase.removeItem(item)
            .andThen(Single.fromCallable {
                mediaList.value.let { list ->
                    if (list != null) {
                        list - item
                    } else emptyList()
                }
            })
            .observeOn(schedulerProvider.main())
            .subscribeFor { newList ->
                submitMediaList(newList)
            }
    }

    // TODO: passing a list snapshot looks a little dirty
    fun onItemMoved(fromPosition: Int, toPosition: Int, listSnapshot: List<Song>) {
        // TODO: NOTE we need to submit the updated list (with updated item positions) because getPlaylistUseCase.moveItem does not trigger the update sometimes
        getPlaylistUseCase.moveItem(fromPosition, toPosition, mediaList.value.orEmpty())
                .observeOn(schedulerProvider.main())
                .doOnComplete { submitMediaList(listSnapshot) }
                .subscribeFor {
                }
    }

    fun onDragEnded() {
        submitMediaList(mediaList.value ?: emptyList())
    }

    /**
     * Do not mess up with [onCreateShortcutOptionSelected] method.
     * This method is intended to create a shortcut for the playlist, not a song.
     */
    fun onCreatePlaylistShortcutActionSelected() {
        val targetPlaylist = playlist.value ?: return
        _confirmPlaylistShortcutCreationEvent.value = targetPlaylist
    }

    fun onCreatePlaylistShortcutActionConfirmed() {
        val targetPlaylist = playlist.value ?: return
        createPlaylistShortcutUseCase.createShortcut(targetPlaylist)
                .observeOn(schedulerProvider.main())
                .doOnComplete { eventLogger.logShortcutCreated(Media.PLAYLIST) }
                .subscribeFor { }
    }

}