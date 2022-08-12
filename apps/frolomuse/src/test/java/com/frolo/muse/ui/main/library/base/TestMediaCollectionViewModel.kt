package com.frolo.muse.ui.main.library.base

import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Media
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider


class TestMediaCollectionViewModel<T> constructor(
        permissionChecker: PermissionChecker,
        getMediaUseCase: GetMediaUseCase<T>,
        getMediaMenuUseCase: GetMediaMenuUseCase<T>,
        clickMediaUseCase: ClickMediaUseCase<T>,
        playMediaUseCase: PlayMediaUseCase<T>,
        shareMediaUseCase: ShareMediaUseCase<T>,
        deleteMediaUseCase: DeleteMediaUseCase<T>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<T>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<T>,
        createShortcutUseCase: CreateShortcutUseCase<T>,
        schedulerProvider: SchedulerProvider,
        appRouter: AppRouter,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<T>(
        permissionChecker,
        getMediaUseCase,
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
) where T: Media