package com.frolo.muse.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.arch.*
import com.frolo.muse.billing.*
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logClickedOnProduct
import com.frolo.muse.logger.logLaunchedBillingFlow
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import javax.inject.Inject


class BillingViewModel @Inject constructor(
    private val frolomuseApp: FrolomuseApp,
    private val billingManager: BillingManager,
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

    val isBuyPremiumOptionVisible: LiveData<Boolean> =
            isPremiumPurchased.map(false) { isPremiumPurchased -> isPremiumPurchased == false }

    val isPlaybackFadingProBadged: LiveData<Boolean> =
            isPremiumPurchased.map(false) { isPremiumPurchased -> isPremiumPurchased == false }

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
        val isPremiumPurchased = isPremiumPurchased.value ?: true
        if (isPremiumPurchased) {
            navigator.openPlaybackFadingParams()
        } else {
            navigator.offerToBuyPremium()
        }
    }

}