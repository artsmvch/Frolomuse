package com.frolo.muse.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.combine
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.billing.ProductId
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.ProductOfferUiElementSource
import com.frolo.muse.logger.logProductOffered
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.rx.SchedulerProvider


/**
 * Base ViewModel with billing stuff. If you need to access the status of
 * product purchases and trials, inherit from this class.
 */
abstract class BillingViewModel constructor(
    private val schedulerProvider: SchedulerProvider,
    private val navigator: Navigator,
    private val billingManager: BillingManager,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    protected val isPremiumPurchased: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            billingManager.isProductPurchased(productId = ProductId.PREMIUM, forceCheckFromApi = true)
                .observeOn(schedulerProvider.main())
                .subscribeFor { isPurchased ->
                    value = isPurchased
                }
        }
    }

    private val premiumTrialStatus: LiveData<TrialStatus> by lazy {
        MutableLiveData<TrialStatus>().apply {
            billingManager.getTrialStatus()
                .observeOn(schedulerProvider.main())
                .subscribeFor { status ->
                    value = status
                }
        }
    }

    protected val isPremiumFeatureAvailable: LiveData<Boolean> =
        combine(isPremiumPurchased, premiumTrialStatus) { isPremiumPurchased, trialStatus ->
            isPremiumPurchased == true || trialStatus is TrialStatus.Activated || isPremiumPurchased == null
        }

    /**
     * Checks if the user has access to premium features. If not, it offers the user to buy premium.
     * [uiElementSource] indicates which UI element calls the premium feature.
     */
    protected fun tryUsePremiumFeature(uiElementSource: ProductOfferUiElementSource): Boolean {
        val isPremiumPurchased = isPremiumPurchased.value ?: false
        val trialStatus = premiumTrialStatus.value
        return if (isPremiumPurchased || trialStatus == TrialStatus.Activated) {
            true
        } else {
            eventLogger.logProductOffered(ProductId.PREMIUM, uiElementSource)
            navigator.offerToBuyPremium(allowTrialActivation = true)
            false
        }
    }

}