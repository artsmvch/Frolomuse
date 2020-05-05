package com.frolo.muse.ui.main.library.genres.genre

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.liveDataOf
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetGenreSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
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
        private val playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        createShortcutUseCase: CreateShortcutUseCase<Song>,
        schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger,
        genreArg: Genre
): AbsSongCollectionViewModel<Song>(
        player,
        getGenreSongsUseCase,
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
) {

    val title: LiveData<String> = liveDataOf(genreArg.name)

    fun onPlayButtonClicked() {
        val items = mediaList.value ?: emptyList()
        playMediaUseCase.play(items).subscribeFor {  }
    }

}