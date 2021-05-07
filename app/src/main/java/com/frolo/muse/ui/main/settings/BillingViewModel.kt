package com.frolo.muse.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.arch.*
import com.frolo.muse.billing.*
import com.frolo.muse.engine.PlaybackFadingStrategy
import com.frolo.muse.engine.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logClickedOnProduct
import com.frolo.muse.logger.logLaunchedBillingFlow
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import javax.inject.Inject


class BillingViewModel @Inject constructor(
    private val frolomuseApp: FrolomuseApp,
    private val player: Player,
    private val billingManager: BillingManager,
    private val preferences: Preferences,
    private val navigator: Navigator,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseAndroidViewModel(frolomuseApp, eventLogger) {

    private val isPremiumPurchased: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            billingManager.isProductPurchased(ProductId.PREMIUM)
                .observeOn(schedulerProvider.main())
                .subscribeFor { isPurchased ->
                    value = isPurchased
                }
        }
    }

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
                        value = params.isEnabled
                    }
                }
        }
    }

    val isBuyPremiumOptionVisible: LiveData<Boolean> =
            isPremiumPurchased.map(false) { isPremiumPurchased -> isPremiumPurchased == false }

    val isPlaybackFadingProBadged: LiveData<Boolean> =
            combine(isPremiumPurchased, hasNonEmptyPlaybackFadingParams) { isPremiumPurchased, hasNonEmptyParams ->
                isPremiumPurchased == false && hasNonEmptyParams != true
            }

    private val _showPremiumBenefitsEvent = SingleLiveEvent<Unit>()
    val showPremiumBenefitsEvent: LiveData<Unit> get() = _showPremiumBenefitsEvent

    fun onBuyPremiumPreferenceClicked() {
        eventLogger.logClickedOnProduct(ProductId.PREMIUM)
        _showPremiumBenefitsEvent.call()
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
        val userHasUsedIt = hasNonEmptyPlaybackFadingParams.value ?: false
        val isPremiumPurchased = isPremiumPurchased.value ?: true
        if (userHasUsedIt || isPremiumPurchased) {
            navigator.openPlaybackFadingParams()
        } else {
            navigator.offerToBuyPremium()
        }
    }

}