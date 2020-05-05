package com.frolo.muse.ui.main.library.genres

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Genre
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import javax.inject.Inject


class GenreListViewModel @Inject constructor(
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
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Genre>(
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
        navigator,
        eventLogger)