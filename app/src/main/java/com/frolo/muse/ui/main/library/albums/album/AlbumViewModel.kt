package com.frolo.muse.ui.main.library.albums.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.arch.liveDataOf
import com.frolo.muse.arch.map
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel


class AlbumViewModel constructor(
        player: Player,
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
        eventLogger: EventLogger,
        private val albumArg: Album
): AbsSongCollectionViewModel<Song>(
        player,
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
) {

    private val _albumId: MutableLiveData<Long> = MutableLiveData(albumArg.id)
    val albumId: LiveData<Long> get() = _albumId

    val albumName: LiveData<String> = liveDataOf(albumArg.name)

    val artistName: LiveData<String> = liveDataOf(albumArg.artist)

    val playButtonVisible: LiveData<Boolean> =
        mediaList.map(initialValue = false) { list ->
            !list.isNullOrEmpty()
        }

    private val _confirmAlbumShortcutCreationEvent = SingleLiveEvent<Album>()
    val confirmAlbumShortcutCreationEvent: LiveData<Album>
        get() = _confirmAlbumShortcutCreationEvent

    fun onAlbumArtClicked() {
        navigator.editAlbum(albumArg)
    }

    fun onPlayButtonClicked() {
        val snapshot = mediaList.value.orEmpty()
        playMediaUseCase.play(snapshot).subscribeFor {  }
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
            .subscribeFor { dispatchShortcutCreated() }
    }

}