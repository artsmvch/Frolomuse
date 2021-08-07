package com.frolo.muse.ui.main.library.mostplayed

import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.media.get.GetMostPlayedSongsUseCase
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.SongWithPlayCount
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * This view model should be notified about View's lifecycle,
 * namely [onStart] and [onStop] should be called at the appropriate time.
 * This is intended to unload the subscription to [GetMostPlayedSongsUseCase.getMediaList] source
 * when user is not currently at this view model, because this source is heavy and may affect:
 * -processor time;
 * -battery life;
 * -user experience;
 *
 * The time when the subscription should be disposed is controlled by [INACTIVITY_TIMEOUT].
 */
class MostPlayedViewModel @Inject constructor(
        player: Player,
        permissionChecker: PermissionChecker,
        getMostPlayedUseCase: GetMostPlayedSongsUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<SongWithPlayCount>,
        clickMediaUseCase: ClickMediaUseCase<SongWithPlayCount>,
        playMediaUseCase: PlayMediaUseCase<SongWithPlayCount>,
        shareMediaUseCase: ShareMediaUseCase<SongWithPlayCount>,
        deleteMediaUseCase: DeleteMediaUseCase<SongWithPlayCount>,
        getIsFavouriteUseCase: GetIsFavouriteUseCase<SongWithPlayCount>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<SongWithPlayCount>,
        createShortcutUseCase: CreateShortcutUseCase<SongWithPlayCount>,
        private val schedulerProvider: SchedulerProvider,
        navigator: Navigator,
        eventLogger: EventLogger
): AbsSongCollectionViewModel<SongWithPlayCount>(
        player,
        permissionChecker,
        getMostPlayedUseCase,
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

    private companion object {
        const val INACTIVITY_TIMEOUT = 5_000L // 5 seconds
    }

    private var subscriptionTimeoutDisposable: Disposable? = null
    private var subscriptionWasCancelled: Boolean = false

    override fun onStart() {
        super.onStart()

        subscriptionTimeoutDisposable?.dispose()

        if (subscriptionWasCancelled) {
            requireSubscription()
            subscriptionWasCancelled = false
        }
    }

    override fun onStop() {
        super.onStop()

        Completable.timer(INACTIVITY_TIMEOUT, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { d ->
                subscriptionTimeoutDisposable?.dispose()
                subscriptionTimeoutDisposable = d
            }
            .subscribeFor {
                cancelSubscription()
                subscriptionWasCancelled = true
            }
    }

}