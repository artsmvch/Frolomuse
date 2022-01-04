package com.frolo.muse.ui.main.library.playlists

import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import javax.inject.Inject


class PlaylistListViewModel @Inject constructor(
        permissionChecker: PermissionChecker,
        getAllPlaylistsUseCase: GetAllMediaUseCase<Playlist>,
        getMediaMenuUseCase: GetMediaMenuUseCase<Playlist>,
        clickMediaUseCase: ClickMediaUseCase<Playlist>,
        playMediaUseCase: PlayMediaUseCase<Playlist>,
        shareMediaUseCase: ShareMediaUseCase<Playlist>,
        deleteMediaUseCase: DeleteMediaUseCase<Playlist>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Playlist>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Playlist>,
        createShortcutUseCase: CreateShortcutUseCase<Playlist>,
        schedulerProvider: SchedulerProvider,
        private val appRouter: AppRouter,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Playlist>(
        permissionChecker,
        getAllPlaylistsUseCase,
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

    fun onCreatePlaylistButtonClicked() {
        appRouter.createPlaylist()
    }

}