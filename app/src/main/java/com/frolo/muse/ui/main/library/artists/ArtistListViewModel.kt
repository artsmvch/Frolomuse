package com.frolo.muse.ui.main.library.artists

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetAllMediaUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Artist
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel
import javax.inject.Inject


class ArtistListViewModel @Inject constructor(
        getAllArtistsUseCase: GetAllMediaUseCase<Artist>,
        getMediaMenuUseCase: GetMediaMenuUseCase<Artist>,
        clickMediaUseCase: ClickMediaUseCase<Artist>,
        playMediaUseCase: PlayMediaUseCase<Artist>,
        shareMediaUseCase: ShareMediaUseCase<Artist>,
        deleteMediaUseCase: DeleteMediaUseCase<Artist>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Artist>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Artist>,
        createShortcutUseCase: CreateShortcutUseCase<Artist>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Artist>(
        getAllArtistsUseCase,
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