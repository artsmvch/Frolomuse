package com.frolo.muse.ui.main.library.genres

import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Genre
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import javax.inject.Inject


class GenreListViewModel @Inject constructor(
        permissionChecker: PermissionChecker,
        getSortableMediaListUseCase: GetAllMediaUseCase<Genre>,
        getMediaMenuUseCase: GetMediaMenuUseCase<Genre>,
        clickMediaUseCase: ClickMediaUseCase<Genre>,
        playMediaUseCase: PlayMediaUseCase<Genre>,
        shareMediaUseCase: ShareMediaUseCase<Genre>,
        deleteMediaUseCase: DeleteMediaUseCase<Genre>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Genre>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Genre>,
        createShortcutUseCase: CreateShortcutUseCase<Genre>,
        schedulerProvider: SchedulerProvider,
        appRouter: AppRouter,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Genre>(
        permissionChecker,
        getSortableMediaListUseCase,
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
        eventLogger)