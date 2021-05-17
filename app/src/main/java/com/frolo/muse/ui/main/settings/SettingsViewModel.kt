package com.frolo.muse.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.BuildConfig
import com.frolo.muse.arch.*
import com.frolo.muse.billing.*
import com.frolo.muse.engine.PlaybackFadingStrategy
import com.frolo.muse.engine.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.ProductOfferUiElementSource
import com.frolo.muse.logger.logLaunchedBillingFlow
import com.frolo.muse.logger.logProductOffered
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BillingViewModel
import javax.inject.Inject


class SettingsViewModel @Inject constructor(
    private val player: Player,
    private val schedulerProvider: SchedulerProvider,
    private val navigator: Navigator,
    private val billingManager: BillingManager,
    private val preferences: Preferences,
    private val eventLogger: EventLogger
): BillingViewModel(schedulerProvider, navigator, billingManager, eventLogger) {

    private val hasNonEmptyPlaybackFadingParams: LiveData<Boolean> by lazy {
        // First, check the current interval in the player, if any
        val isCurrentIntervalPositive = player.getPlaybackFadingStrategy()
            ?.let { PlaybackFadingStrategy.getInterval(it) }
            .let { interval -> interval != null && interval > 0 }

        MutableLiveData<Boolean>(isCurrentIntervalPositive).apply {
            // Then, check the params from the preferences
            preferences.playbackFadingParams
                .observeOn(schedulerProvider.main())
                .subscribeFor { params ->
                    if (value != true) {
                        //value = params.isEnabled
                    }
                }
        }
    }

    val isBuyPremiumOptionVisible: LiveData<Boolean> =
            isPremiumPurchased.map(false) { isPremiumPurchased -> isPremiumPurchased == false }

    val isPlaybackFadingProBadged: LiveData<Boolean> =
            combine(isPremiumFeatureAvailable, hasNonEmptyPlaybackFadingParams) { isPremiumFeatureAvailable, hasNonEmptyParams ->
                isPremiumFeatureAvailable == false && hasNonEmptyParams != true
            }

    private val _notifyPremiumProductConsumedEvent = SingleLiveEvent<Unit>()
    val notifyPremiumProductConsumedEvent: LiveData<Unit> get() = _notifyPremiumProductConsumedEvent

    private val _notifyPremiumTrialResetEvent = SingleLiveEvent<Unit>()
    val notifyPremiumTrialResetEvent: LiveData<Unit> get() = _notifyPremiumTrialResetEvent

    fun onBuyPremiumPreferenceClicked() {
        eventLogger.logProductOffered(ProductId.PREMIUM, ProductOfferUiElementSource.SETTINGS)
        navigator.offerToBuyPremium()
    }

    fun onBuyPremiumClicked() {
        val productId = ProductId.PREMIUM
        eventLogger.logLaunchedBillingFlow(productId)
        billingManager.launchBillingFlow(productId)
            .observeOn(schedulerProvider.main())
            .subscribeFor { result ->
                result?.debugMessage
            }
    }

    fun onPlaybackFadingClick() {
        // Playback fading has a special logic: the user may have used it before,
        // in which case we have to let him continue to use it.
        val userHasUsedIt: Boolean = hasNonEmptyPlaybackFadingParams.value ?: false
        if (userHasUsedIt || tryUsePremiumFeature(ProductOfferUiElementSource.PLAYBACK_FADING)) {
            navigator.openPlaybackFadingParams()
        }
    }

    /**
     * [!] For debugging only.
     */
    fun onConsumePremiumProductClicked() {
        assertNotDebug("Consume premium product")
        billingManager.consumeProduct(ProductId.PREMIUM)
            .observeOn(schedulerProvider.main())
            .subscribeFor {
                _notifyPremiumProductConsumedEvent.call()
            }
    }

    /**
     * [!] For debugging only.
     */
    fun onResetPremiumTrialClicked() {
        assertNotDebug("Reset premium trial")
        billingManager.resetTrial()
            .observeOn(schedulerProvider.main())
            .subscribeFor {
                _notifyPremiumTrialResetEvent.call()
            }
    }

    private fun assertNotDebug(option: String) {
        if (!BuildConfig.DEBUG) {
            val msg = "How the hell did the '$option' option end up in Production"
            throw IllegalStateException(msg)
        }
    }

}