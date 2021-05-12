package com.frolo.muse.ui.main.settings.premium

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.combine
import com.frolo.muse.billing.BillingManager
import com.frolo.muse.billing.ProductDetails
import com.frolo.muse.billing.ProductId
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logFailedToGetProductDetails
import com.frolo.muse.logger.logLaunchedBillingFlow
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import javax.inject.Inject


class BuyPremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    val productDetails: LiveData<ProductDetails> by lazy {
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

    val isBuyButtonEnabled: LiveData<Boolean> =
        combine(isLoading, productDetails) { isLoading, productDetails ->
            isLoading != true && productDetails != null
        }

    fun onBuyClicked() {
        val productId = ProductId.PREMIUM
        eventLogger.logLaunchedBillingFlow(productId)
        billingManager.launchBillingFlow(productId)
            .observeOn(schedulerProvider.main())
            .subscribeFor {  }
    }

}