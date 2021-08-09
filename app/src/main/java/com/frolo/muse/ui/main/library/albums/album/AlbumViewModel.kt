package com.frolo.muse.ui.main.library.albums.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.*
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logShortcutCreated
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import com.frolo.muse.ui.main.library.base.AssociatedWithMediaItem


class AlbumViewModel constructor(
        player: Player,
        permissionChecker: PermissionChecker,
        getAlbumSongsUseCase: GetAlbumSongsUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        private val playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        createSongShortcutUseCase: CreateShortcutUseCase<Song>,
        private val createAlbumShortcutUseCase: CreateShortcutUseCase<Album>,
        private val schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        private val eventLogger: EventLogger,
        private val albumArg: Album
): AbsSongCollectionViewModel<Song>(
        player,
        permissionChecker,
        getAlbumSongsUseCase,
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
), AssociatedWithMediaItem by AssociatedWithMediaItem(albumArg) {

    private val _albumId: MutableLiveData<Long> = MutableLiveData(albumArg.id)
    val albumId: LiveData<Long> get() = _albumId

    val albumName: LiveData<String> = liveDataOf(albumArg.name)

    val artistName: LiveData<String> = liveDataOf(albumArg.artist)

    private val headerScrollFactor = MutableLiveData<Float>(0f)

    val playButtonVisible: LiveData<Boolean> =
        combine(headerScrollFactor, mediaList) { scrollFactor: Float?, list: List<*>? ->
            if (scrollFactor == null || scrollFactor > 0.3f)
                // If the scroll factor is more than 0.3 then the play button is always hidden
                return@combine false

            // The play button may be visible only if the media list is not empty
            return@combine !list.isNullOrEmpty()
        }

    private val _confirmAlbumShortcutCreationEvent = SingleLiveEvent<Album>()
    val confirmAlbumShortcutCreationEvent: LiveData<Album>
        get() = _confirmAlbumShortcutCreationEvent

    fun onAlbumArtClicked() {
        navigator.editAlbum(albumArg)
    }

    fun onPlayButtonClicked() {
        val snapshot = mediaList.value.orEmpty()
        playMediaUseCase.play(snapshot, associatedMediaItem).subscribeFor {  }
    }

    /**
     * This method should be called when the album header scrolls with the given [scrollFactor].
     * The scroll factor describes how much the header scrolls up.
     * 0.0f means that the header is completely expanded and 1.0f means that the header is completely hidden.
     */
    fun onHeaderScrolled(scrollFactor: Float) {
        headerScrollFactor.value = scrollFactor
    }

    /**
     * Do not mess up with [onCreateShortcutOptionSelected] method.
     * This method is intended to create a shortcut for the album, not a song.
     */
    fun onCreateAlbumShortcutActionSelected() {
        _confirmAlbumShortcutCreationEvent.value = albumArg
    }

    fun onCreateAlbumShortcutActionConfirmed() {
        createAlbumShortcutUseCase.createShortcut(albumArg)
            .observeOn(schedulerProvider.main())
            .doOnComplete { eventLogger.logShortcutCreated(Media.ALBUM) }
            .subscribeFor { }
    }

}