package com.frolo.muse.ui.main.settings.premium

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.EventLiveData
import com.frolo.muse.arch.call
import com.frolo.muse.arch.combine
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.billing.ProductDetails
import com.frolo.muse.billing.ProductId
import com.frolo.muse.billing.TrialStatus
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logFailedToGetProductDetails
import com.frolo.muse.logger.logLaunchedBillingFlow
import com.frolo.muse.logger.logPremiumTrialActivated
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class BuyPremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _closeEvent = EventLiveData<Unit>()
    val closeEvent: LiveData<Unit> get() = _closeEvent

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val productDetails: LiveData<ProductDetails> by lazy {
        MutableLiveData<ProductDetails>().apply {
            billingManager.getProductDetails(ProductId.PREMIUM)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isLoading.value = true }
                .doFinally { _isLoading.value = false }
                .doOnError { eventLogger.logFailedToGetProductDetails(ProductId.PREMIUM) }
                .subscribeFor { productDetails ->
                    value = productDetails
                }
        }
    }

    private val trialStatus: LiveData<TrialStatus> by lazy {
        MutableLiveData<TrialStatus>().apply {
            billingManager.getTrialStatus()
                .observeOn(schedulerProvider.main())
                .subscribeFor { trialStatus ->
                    value = trialStatus
                }
        }
    }

    val premiumStatus: LiveData<PremiumStatus> by lazy {
        combine(productDetails, trialStatus) { productDetails, trialStatus ->
            PremiumStatus(productDetails, trialStatus)
        }
    }

    val isButtonEnabled: LiveData<Boolean> by lazy {
        combine(isLoading, premiumStatus) { isLoading, premiumStatus ->
            isLoading != true && premiumStatus != null
        }
    }

    fun onButtonClicked() {
        val premiumStatus: PremiumStatus = premiumStatus.value ?: return
        if (premiumStatus.activatePremium) {
            eventLogger.logPremiumTrialActivated()
            billingManager.activateTrialVersion()
                .observeOn(schedulerProvider.main())
                .subscribeFor { _closeEvent.call() }
        } else {
            val productId = ProductId.PREMIUM
            eventLogger.logLaunchedBillingFlow(productId)
            billingManager.launchBillingFlow(productId)
                .observeOn(schedulerProvider.main())
                .subscribeFor { _closeEvent.call() }
        }
    }

    data class PremiumStatus(
        val productDetails: ProductDetails?,
        val trialStatus: TrialStatus?
    ) {
        val activatePremium: Boolean
            get() = trialStatus is TrialStatus.Available
    }

}