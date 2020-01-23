package com.frolo.muse.ui.main.library.mostplayed

import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMostPlayedSongsUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.SongWithPlayCount
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import javax.inject.Inject


class MostPlayedViewModel @Inject constructor(
        player: Player,
        getMostPlayedUseCase: GetMostPlayedSongsUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<SongWithPlayCount>,
        clickMediaUseCase: ClickMediaUseCase<SongWithPlayCount>,
        playMediaUseCase: PlayMediaUseCase<SongWithPlayCount>,
        shareMediaUseCase: ShareMediaUseCase<SongWithPlayCount>,
        deleteMediaUseCase: DeleteMediaUseCase<SongWithPlayCount>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<SongWithPlayCount>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<SongWithPlayCount>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsSongCollectionViewModel<SongWithPlayCount>(
        player,
        getMostPlayedUseCase,
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
)