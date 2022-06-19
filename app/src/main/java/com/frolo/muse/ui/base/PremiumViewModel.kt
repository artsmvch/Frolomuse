package com.frolo.muse.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.combine
import com.frolo.muse.billing.Products
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.interactor.billing.PremiumManager
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.ProductOfferUiElementSource
import com.frolo.muse.logger.logProductOffered
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider


/**
 * Base ViewModel with billing stuff. If you need to access the status of
 * product purchases and trials, inherit from this class.
 */
abstract class PremiumViewModel constructor(
    private val schedulerProvider: SchedulerProvider,
    private val appRouter: AppRouter,
    private val premiumManager: PremiumManager,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    protected val isPremiumPurchased: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            premiumManager.isProductPurchased(productId = Products.PREMIUM, forceCheckFromApi = true)
                .observeOn(schedulerProvider.main())
                .subscribeFor { isPurchased ->
                    value = isPurchased
                }
        }
    }

    private val premiumTrialStatus: LiveData<TrialStatus> by lazy {
        MutableLiveData<TrialStatus>().apply {
            premiumManager.getTrialStatus()
                .observeOn(schedulerProvider.main())
                .subscribeFor { status ->
                    value = status
                }
        }
    }

    protected val isPremiumFeatureAvailable: LiveData<Boolean> by lazy {
        combine(isPremiumPurchased, premiumTrialStatus) { isPremiumPurchased, trialStatus ->
            isPremiumPurchased == true || trialStatus is TrialStatus.Activated
        }
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
            eventLogger.logProductOffered(Products.PREMIUM, uiElementSource)
            appRouter.offerToBuyPremium(allowTrialActivation = true)
            false
        }
    }

}