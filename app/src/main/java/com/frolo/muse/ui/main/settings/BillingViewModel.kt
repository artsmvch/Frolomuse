package com.frolo.muse.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.call
import com.frolo.muse.arch.combineMultiple
import com.frolo.muse.billing.*
import com.frolo.muse.interactor.feature.FeaturesUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logClickedOnProduct
import com.frolo.muse.logger.logLaunchedBillingFlow
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import javax.inject.Inject


class BillingViewModel @Inject constructor(
    private val frolomuseApp: FrolomuseApp,
    private val featuresUseCase: FeaturesUseCase,
    private val billingManager: BillingManager,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseAndroidViewModel(frolomuseApp, eventLogger) {

    private val isPurchaseFeatureEnabled: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false).apply {
            featuresUseCase.isPurchaseFeatureEnabled()
                .observeOn(schedulerProvider.main())
                .subscribeFor { isEnabled ->
                    value = isEnabled
                }
        }
    }

    private val _isPremiumPurchased = MutableLiveData<Boolean>().apply {
        billingManager.isProductPurchased(ProductId.PREMIUM)
            .observeOn(schedulerProvider.main())
            .subscribeFor { isPurchased ->
                value = isPurchased
            }
    }
    val isPremiumPurchased: LiveData<Boolean> get() = _isPremiumPurchased

    val isBuyPremiumOptionVisible: LiveData<Boolean> =
        combineMultiple(isPurchaseFeatureEnabled, isPremiumPurchased) { booleans ->
            val isFeatureEnabled = booleans[0] ?: false
            val isPurchased = booleans[1]
            isFeatureEnabled && (isPurchased != null && !isPurchased)
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

}