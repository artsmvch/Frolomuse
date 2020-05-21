package com.frolo.muse.ui.main.library.genres.genre

import androidx.lifecycle.LiveData
import com.frolo.muse.arch.SingleLiveEvent
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
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel


class GenreViewModel constructor(
        player: Player,
        permissionChecker: PermissionChecker,
        getGenreSongsUseCase: GetGenreSongsUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        private val playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        createSongShortcutUseCase: CreateShortcutUseCase<Song>,
        private val createGenreShortcutUseCase: CreateShortcutUseCase<Genre>,
        private val schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger,
        private val genreArg: Genre
): AbsSongCollectionViewModel<Song>(
        player,
        permissionChecker,
        getGenreSongsUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        getIsFavouriteUseCase,
        changeFavouriteUseCase,
        createSongShortcutUseCase,
        schedulerProvider,
        navigator,
        eventLogger
) {

    val title: LiveData<String> = liveDataOf(genreArg.name)

    private val _confirmGenreShortcutCreationEvent = SingleLiveEvent<Genre>()
    val confirmGenreShortcutCreationEvent: LiveData<Genre>
        get() = _confirmGenreShortcutCreationEvent

    fun onPlayButtonClicked() {
        val items = mediaList.value ?: emptyList()
        playMediaUseCase.play(items).subscribeFor {  }
    }

    /**
     * Do not mess up with [onCreateShortcutOptionSelected] method.
     * This method is intended to create a shortcut for the genre, not a song.
     */
    fun onCreateGenreShortcutActionSelected() {
        _confirmGenreShortcutCreationEvent.value = genreArg
    }

    fun onCreateGenreShortcutActionConfirmed() {
        createGenreShortcutUseCase.createShortcut(genreArg)
                .observeOn(schedulerProvider.main())
                .subscribeFor { dispatchShortcutCreated() }
    }

}