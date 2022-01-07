package com.frolo.muse.ui.main.library.genres.genre

import androidx.lifecycle.LiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.liveDataOf
import com.frolo.player.Player
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetGenreSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logShortcutCreated
import com.frolo.music.model.Genre
import com.frolo.music.model.Media
import com.frolo.music.model.Song
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import com.frolo.muse.ui.main.library.base.AssociatedWithMediaItem


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
    appRouter: AppRouter,
    private val eventLogger: EventLogger,
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
        appRouter,
        eventLogger
), AssociatedWithMediaItem by AssociatedWithMediaItem(genreArg) {

    val title: LiveData<String> = liveDataOf(genreArg.name)

    private val _confirmGenreShortcutCreationEvent = SingleLiveEvent<Genre>()
    val confirmGenreShortcutCreationEvent: LiveData<Genre>
        get() = _confirmGenreShortcutCreationEvent

    fun onPlayButtonClicked() {
        val items = mediaList.value ?: emptyList()
        playMediaUseCase.play(items, associatedMediaItem).subscribeFor {  }
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
                .doOnComplete { eventLogger.logShortcutCreated(Media.GENRE) }
                .subscribeFor { }
    }

}