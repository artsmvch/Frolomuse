package com.frolo.muse.ui.main.library.recent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetRecentlyAddedSongUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.Recently
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.menu.RecentPeriodMenu
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import javax.inject.Inject


class RecentlyAddedSongListViewModel @Inject constructor(
        player: Player,
        permissionChecker: PermissionChecker,
        private val getRecentlyAddedSongUseCase: GetRecentlyAddedSongUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        createShortcutUseCase: CreateShortcutUseCase<Song>,
        private val schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsSongCollectionViewModel<Song>(
        player,
        permissionChecker,
        getRecentlyAddedSongUseCase,
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

    private val _openRecentPeriodMenuEvent: MutableLiveData<RecentPeriodMenu> = SingleLiveEvent()
    val openRecentPeriodMenuEvent: LiveData<RecentPeriodMenu> get() = _openRecentPeriodMenuEvent

    fun onRecentPeriodOptionSelected() {
        getRecentlyAddedSongUseCase.getRecentPeriodMenu()
                .observeOn(schedulerProvider.main())
                .subscribeFor { menu ->
                    _openRecentPeriodMenuEvent.value = menu
                }
    }

    fun onPeriodSelected(@Recently.Period period: Int) {
        getRecentlyAddedSongUseCase.applyPeriod(period)
                .observeOn(schedulerProvider.main())
                .subscribeFor { list ->
                    submitMediaList(list)
                }
    }

}