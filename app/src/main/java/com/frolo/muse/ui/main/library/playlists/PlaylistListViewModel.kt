package com.frolo.muse.ui.main.library.playlists

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import javax.inject.Inject


class PlaylistListViewModel @Inject constructor(
        getAllPlaylistsUseCase: GetAllMediaUseCase<Playlist>,
        getMediaMenuUseCase: GetMediaMenuUseCase<Playlist>,
        clickMediaUseCase: ClickMediaUseCase<Playlist>,
        playMediaUseCase: PlayMediaUseCase<Playlist>,
        shareMediaUseCase: ShareMediaUseCase<Playlist>,
        deleteMediaUseCase: DeleteMediaUseCase<Playlist>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Playlist>,
        schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Playlist>(
        getAllPlaylistsUseCase,
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

    fun onCreatePlaylistButtonClicked() {
        navigator.createPlaylist()
    }

}