package com.frolo.muse.ui.main.library.albums

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import javax.inject.Inject


class AlbumListViewModel @Inject constructor(
        getMediaListUseCase: GetAllMediaUseCase<Album>,
        getMediaMenuUseCase: GetMediaMenuUseCase<Album>,
        clickMediaUseCase: ClickMediaUseCase<Album>,
        playMediaUseCase: PlayMediaUseCase<Album>,
        shareMediaUseCase: ShareMediaUseCase<Album>,
        deleteMediaUseCase: DeleteMediaUseCase<Album>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Album>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Album>,
        createShortcutUseCase: CreateShortcutUseCase<Album>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Album>(
        getMediaListUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        getIsFavouriteUseCase,
        changeFavouriteUseCase,
        createShortcutUseCase,
        schedulerProvider,
        navigator,
        eventLogger
)