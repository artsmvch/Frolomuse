package com.frolo.muse.ui.main.settings.donations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.billing.BillingFlowSuccess
import com.frolo.billing.BillingManager
import com.frolo.billing.ProductDetails
import com.frolo.muse.BuildInfo
import com.frolo.arch.support.EventLiveData
import com.frolo.arch.support.call
import com.frolo.muse.billing.Products
import com.frolo.muse.logger.*
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single
import io.reactivex.functions.Function
import javax.inject.Inject


class DonationsViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val appRouter: AppRouter,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
) : BaseViewModel(eventLogger) {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    internal val donationItems: LiveData<List<DonationItem>> by lazy {
        MutableLiveData<List<DonationItem>>().apply {
            getDonationItemsSource()
                .observeOn(schedulerProvider.main())
                .doOnError { err -> eventLogger.logFailedToLoadDonations(err) }
                .doOnSubscribe { _isLoading.value = true }
                .doOnSuccess { _isLoading.value = false }
                .subscribeFor { value = it }
        }
    }

    private val _thanksForDonationEvent = EventLiveData<Unit>()
    val thanksForDonationEvent: LiveData<Unit> get() = _thanksForDonationEvent

    private fun getDonationItemsSource(): Single<List<DonationItem>> {
        val productIds = listOf(
            Products.DONATE_THANKS,
            Products.DONATE_COFFEE,
            Products.DONATE_MOVIE_TICKET,
            Products.DONATE_PIZZA,
            Products.DONATE_MEAL,
            Products.DONATE_GYM_MEMBERSHIP
        )

        val productDetailsSources = productIds.mapIndexed { _, productId ->
            billingManager.getProductDetails(productId)
                .flatMap { product ->
                    // Make sure it is consumed so that the user can buy it again
                    billingManager.consumeProduct(productId)
                        .doOnError { error ->
                            eventLogger.logFailedToConsumeDonation(productId, error)
                            logError(error)
                        }
                        .onErrorComplete()
                        .andThen(Single.just(product))
                }
                .doOnError { err ->
                    eventLogger.logFailedToLoadDonation(productId, err)
                    logError(err)
                }
                .map { product -> listOf<ProductDetails>(product) }
                .onErrorReturnItem(emptyList())
        }

        val zipper = Function<Array<out Any>, List<ProductDetails>> { array ->
            array.flatMap { it as List<ProductDetails> }
        }

        return Single.zip(productDetailsSources, zipper)
            .observeOn(schedulerProvider.computation())
            .map { productDetailsList ->
                DonationItemsFactory.createDonationItems(productDetailsList)
            }
    }

    fun onFirstCreate() {
        eventLogger.logDonationsOpened()
    }

    internal fun onDonationItemClicked(item: DonationItem) {
        if (item is DonationItem.Purchase) {
            val productId = item.productDetails.productId
            billingManager.launchBillingFlowForResult(productId)
                .flatMap { result ->
                    // Consume it so that the user can buy it later again
                    billingManager.consumeProduct(result.productId)
                        .doOnError { error ->
                            eventLogger.logFailedToConsumeDonation(result.productId, error)
                            logError(error)
                        }
                        .onErrorComplete()
                        .andThen(Single.just(result))
                }
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { eventLogger.logDonationPurchaseClicked(productId) }
                .subscribeFor { result ->
                    if (result is BillingFlowSuccess) {
                        _thanksForDonationEvent.call()
                    }
                }
        } else if (item is DonationItem.Rating) {
            eventLogger.logDonationRatingClicked()
            appRouter.goToStore()
        } else {
            if (BuildInfo.isDebug()) {
                throw IllegalArgumentException("Unexpected donation item: $item")
            }
        }
    }

}