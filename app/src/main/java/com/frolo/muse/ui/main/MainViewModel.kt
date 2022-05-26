package com.frolo.muse.ui.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.EventLiveData
import com.frolo.muse.arch.call
import com.frolo.muse.player.PlayerStateRestorer
import com.frolo.muse.player.PlayerWrapper
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.interactor.feature.FeaturesUseCase
import com.frolo.muse.interactor.firebase.SyncFirebaseMessagingTokenUseCase
import com.frolo.muse.interactor.media.TransferPlaylistsUseCase
import com.frolo.muse.interactor.media.shortcut.NavigateToMediaUseCase
import com.frolo.muse.interactor.player.OpenAudioSourceUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.permission.PermissionChecker
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.PlayerHostViewModel
import com.frolo.music.model.Media
import com.frolo.player.Player
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * The main view model associated with the root screen of the app.
 *
 * The main purpose of this view model is:
 * 1) check the RES permission (Read External Storage);
 * 2) handle connection to the player;
 * 3) handle intents;
 */
class MainViewModel @Inject constructor(
    application: Application,
    playerWrapper: PlayerWrapper,
    private val playerStateRestorer: PlayerStateRestorer,
    private val openAudioSourceUseCase: OpenAudioSourceUseCase,
    private val navigateToMediaUseCase: NavigateToMediaUseCase,
    private val syncFirebaseMessagingTokenUseCase: SyncFirebaseMessagingTokenUseCase,
    private val transferPlaylistsUseCase: TransferPlaylistsUseCase,
    private val featuresUseCase: FeaturesUseCase,
    private val premiumManager: PremiumManager,
    private val schedulerProvider: SchedulerProvider,
    private val permissionChecker: PermissionChecker,
    private val appearancePreferences: AppearancePreferences,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val eventLogger: EventLogger
): PlayerHostViewModel(application, playerWrapper, eventLogger) {

    private var awaitingRESPermissionResult: Boolean = false
    private var lastRESPermissionDenialTime: Long = 0

    private var pendingAudioSourceIntent: String? = null

    private val _requestRESPermissionsEvent = EventLiveData<Unit>()
    val requestRESPermissionsEvent: LiveData<Unit> get() = _requestRESPermissionsEvent

    private val _explainNeedForRESPermissionEvent = EventLiveData<Unit>()
    val explainNeedForRESPermissionEvent: LiveData<Unit> get() = _explainNeedForRESPermissionEvent

    private val _openPermissionSettingsEvent = EventLiveData<Unit>()
    val openPermissionSettingsEvent: LiveData<Unit> get() = _openPermissionSettingsEvent

    val isSnowfallEnabled: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            val source1 = remoteConfigRepository.isSnowfallFeatureEnabled().toFlowable()
            val source2 = appearancePreferences.isSnowfallEnabled()
            val combined = Flowable.combineLatest(source1, source2) { isFeatureEnabled, isLocallyEnabled ->
                isFeatureEnabled && isLocallyEnabled
            }
            combined.distinctUntilChanged()
                .observeOn(schedulerProvider.main())
                .subscribeFor { isEnabled ->
                    value = isEnabled
                }
        }
    }

    private fun tryRestorePlayerStateIfNeeded() {
        val safePlayer: Player = this.player ?: return
        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            tryRequestRESPermission()
            return
        }
        playerStateRestorer
            .restorePlayerStateIfNeeded(safePlayer)
            .observeOn(schedulerProvider.main())
            .subscribe(
                { /* stub */ },
                { err ->
                    if (err is SecurityException) {
                        tryRequestRESPermission()
                    }
                }
            )
            .save()
    }

    private fun tryHandlePendingAudioSourceIntentIfNeeded() {
        val safeSource: String = pendingAudioSourceIntent ?: return
        val safePlayer: Player = this.player ?: return
        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            tryRequestRESPermission()
            return
        }
        openAudioSourceUseCase.openAudioSource(safePlayer, safeSource)
            .observeOn(schedulerProvider.main())
            .subscribeFor { pendingAudioSourceIntent = null }
    }

    private fun tryTransferPlaylistsIfNecessary() {
        transferPlaylistsUseCase.transferPlaylistsIfNecessary()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    /**
     * The view model must call this method to request the RES permission.
     * It also checks if the view model is still pending for the result
     * of the RES permission requested earlier.
     */
    private fun tryRequestRESPermission() {
        if (awaitingRESPermissionResult) {
            return
        }
        if (currentTimeMillis() - lastRESPermissionDenialTime < RES_PERMISSION_REQUEST_THROTTLING_MILLIS) {
            // We don't want to spam these requests
            return
        }
        _requestRESPermissionsEvent.call()
        awaitingRESPermissionResult = true
    }

    fun onFirstCreate() {
        // Syncing Firebase CM
        syncFirebaseMessagingTokenUseCase.sync()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
        // Syncing features
        featuresUseCase.sync()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
        // Syncing premium state
        premiumManager.sync()
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
        // Transfer playlists if necessary
        if (permissionChecker.isQueryMediaContentPermissionGranted) {
            tryTransferPlaylistsIfNecessary()
        }
    }

    fun onStart() {
        if (!permissionChecker.isQueryMediaContentPermissionGranted) {
            tryRequestRESPermission()
        }
    }

    fun onStop() {
    }

    override fun onPlayerConnected(player: Player) {
        super.onPlayerConnected(player)
        tryRestorePlayerStateIfNeeded()
    }

    //region Read Storage Permission

    fun onRESPermissionGranted() {
        awaitingRESPermissionResult = false
        tryRestorePlayerStateIfNeeded()
        tryHandlePendingAudioSourceIntentIfNeeded()
        tryTransferPlaylistsIfNecessary()
    }

    fun onRESPermissionDenied() {
        awaitingRESPermissionResult = false
        _explainNeedForRESPermissionEvent.call()
    }

    fun onAgreedWithRESPermissionExplanation() {
        if (permissionChecker.shouldRequestMediaPermissionInSettings()) {
            _openPermissionSettingsEvent.call()
        } else {
            tryRequestRESPermission()
        }
    }

    fun onDeniedRESPermissionExplanation() {
        lastRESPermissionDenialTime = currentTimeMillis()
    }

    //endregion

    fun onNavigateToMediaIntent(@Media.Kind kindOfMedia: Int, mediaId: Long) {
        navigateToMediaUseCase.navigate(kindOfMedia, mediaId)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

    fun onOpenAudioSourceIntent(source: String) {
        pendingAudioSourceIntent = source
        tryHandlePendingAudioSourceIntentIfNeeded()
    }

    companion object {
        private val RES_PERMISSION_REQUEST_THROTTLING_MILLIS = TimeUnit.MINUTES.toMillis(1)

        private fun currentTimeMillis(): Long = System.currentTimeMillis()
    }
}