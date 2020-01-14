package com.frolo.muse.ui.main.library.genres.genre

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetGenreSongsUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Genre
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel


class GenreViewModel constructor(
        player: Player,
        getGenreSongsUseCase: GetGenreSongsUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger,
        genreArg: Genre
): AbsSongCollectionViewModel(
        player,
        getGenreSongsUseCase,
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

    private val _title by lazy { MutableLiveData(genreArg.name) }
    val title: LiveData<String> get() = _title

}