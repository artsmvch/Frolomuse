package com.frolo.billing.playstore

import com.android.billingclient.api.*
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private fun getMainThreadScheduler(): Scheduler {
    return AndroidSchedulers.mainThread()
}

internal fun BillingClient.queryProductDetailsSingle(
    productIds: List<String>,
    @BillingClient.ProductType type: String
): Single<List<ProductDetails>> {
    val singleSource: Single<List<ProductDetails>> = Single.create { emitter ->
        val products = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(type)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        queryProductDetailsAsync(params) { result: BillingResult, queryResult: QueryProductDetailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                emitter.onSuccess(queryResult.productDetailsList)
            } else {
                emitter.onError(BillingClientException(result))
            }
        }
    }
    val scheduler: Scheduler = getMainThreadScheduler()
    return singleSource
        .subscribeOn(scheduler)
        .unsubscribeOn(scheduler)
        .observeOn(scheduler)
}

internal fun BillingClient.queryPurchasesSingle(@BillingClient.ProductType type: String): Single<PurchasesResult> {
    return coroutineToSingle {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(type)
            .build()
        queryPurchasesAsync(params)
    }
}

private fun <T : Any> coroutineToSingle(callable: suspend () -> T): Single<T> {
    val source = Single.create<T> { emitter ->
        GlobalScope.launch {
            kotlin.runCatching {
                callable.invoke()
            }.onSuccess {
                emitter.onSuccess(it)
            }.onFailure {
                emitter.onError(it)
            }
        }
    }
    return source
        .subscribeOn(getMainThreadScheduler())
        .unsubscribeOn(getMainThreadScheduler())
        .observeOn(getMainThreadScheduler())
}
