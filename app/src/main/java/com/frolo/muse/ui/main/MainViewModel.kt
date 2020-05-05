package com.frolo.muse.ui.main

import androidx.lifecycle.LiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.engine.Player
import com.frolo.muse.interactor.media.shortcut.NavigateToMediaUseCase
import com.frolo.muse.interactor.player.RestorePlayerStateUseCase
import com.frolo.muse.interactor.rate.RateUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Media
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.disposables.Disposable
import javax.inject.Inject


class MainViewModel @Inject constructor(
     private val rateUseCase: RateUseCase,
     private val restorePlayerStateUseCase: RestorePlayerStateUseCase,
     private val navigateToMediaUseCase: NavigateToMediaUseCase,
     private val schedulerProvider: SchedulerProvider,
     private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    // Internal
    private var _player: Player? = null
    private var _permissionAsked: Boolean = false
    private var _permissionGranted: Boolean = false

    private var askToRateDisposable: Disposable? = null

    private val _askReadStoragePermissionsEvent by lazy {
        SingleLiveEvent<Unit>().apply { call() }
    }
    val askReadStoragePermissionsEvent: LiveData<Unit>
        get() = _askReadStoragePermissionsEvent

    private val _askToRateEvent = SingleLiveEvent<Unit>()
    val askToRateEvent: LiveData<Unit> get() = _askToRateEvent

    private fun checkPlayerAndPermission() {
        _player?.also { safePlayer ->
            _permissionGranted.also { granted ->
                if (granted) {
                    restorePlayerStateUseCase
                            .restorePlayerStateIfNeeded(safePlayer)
                            .observeOn(schedulerProvider.main())
                            .subscribe(
                                    {
                                    },
                                    { err ->
                                        if (err is SecurityException) {
                                            askPermissionIfNotAskedYet()
                                        }
                                    }
                            )
                            .save()
                } else {
                    // Permission not granted, ask for it then
                    askPermissionIfNotAskedYet()
                }
            }
        }
    }

    private fun askPermissionIfNotAskedYet() {
        if (!_permissionAsked) {
            _permissionAsked = true
            _askReadStoragePermissionsEvent.call()
        }
    }

    fun onResume() {
        rateUseCase
                .checkIfRateNeeded()
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { d ->
                    askToRateDisposable?.dispose()
                    askToRateDisposable = d
                }
                .subscribeFor { needRate ->
                    if (needRate) {
                        _askToRateEvent.call()
                    }
                }
    }

    fun onPause() {
        askToRateDisposable?.dispose()
    }

    fun onDismissRate() {
        rateUseCase.dismissRate()
    }

    fun onApproveToRate() {
        rateUseCase.approveRate()
    }

    fun onWishingAskingLater() {
        rateUseCase.askLater()
    }

    fun onCancelledRateDialog() {
        rateUseCase.cancelRate()
    }

    fun onPlayerConnected(player: Player) {
        _player = player
        checkPlayerAndPermission()
    }

    fun onPlayerDisconnected() {
        _player = null
    }

    fun onReadStoragePermissionGranted() {
        _permissionGranted = true
        checkPlayerAndPermission()
    }

    fun onReadStoragePermissionDenied() {
        _permissionGranted = false
    }

    fun onNavigateToMediaIntent(@Media.Kind kindOfMedia: Int, mediaId: Long) {
        navigateToMediaUseCase.navigate(kindOfMedia, mediaId)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    override fun onCleared() {
        super.onCleared()
        askToRateDisposable?.dispose()
    }
}