package com.frolo.muse.ui.main.library.artists.artist.albums

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.get.GetAlbumsOfArtistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsMediaCollectionViewModel


class AlbumsOfArtistViewModel constructor(
        getAlbumsOfArtistUseCase: GetAlbumsOfArtistUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Album>,
        clickMediaUseCase: ClickMediaUseCase<Album>,
        playMediaUseCase: PlayMediaUseCase<Album>,
        shareMediaUseCase: ShareMediaUseCase<Album>,
        deleteMediaUseCase: DeleteMediaUseCase<Album>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Album>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsMediaCollectionViewModel<Album>(
        getAlbumsOfArtistUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        changeFavouriteUseCase,
        schedulerProvider,
        navigator,
        eventLogger
)