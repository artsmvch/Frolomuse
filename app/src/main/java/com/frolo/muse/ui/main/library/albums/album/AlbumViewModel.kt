package com.frolo.muse.ui.main.library.albums.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAlbumSongsUseCase
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
        playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        schedulerProvider: SchedulerProvider,
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
        schedulerProvider,
        navigator,
        eventLogger
) {

    private val _title: MutableLiveData<String> = MutableLiveData()
    val title: LiveData<String> = _title

    private val _albumId: MutableLiveData<Long> = MutableLiveData()
    val albumId: LiveData<Long> = _albumId

    init {
        _title.value = albumArg.name
        _albumId.value = albumArg.id
    }

    fun onEditAlbumOptionSelected() {
        navigator.editAlbum(albumArg)
    }

    fun onAlbumUpdated(previousAlbum: Album, updatedAlbum: Album) {
        if (previousAlbum == albumArg) {
            _title.value = updatedAlbum.name
            _albumId.value = updatedAlbum.id
        }
    }

}