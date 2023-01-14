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

internal fun BillingClient.querySkuDetailsSingle(skuList: List<String>, @BillingClient.SkuType type: String): Single<List<SkuDetails>> {
    val singleSource: Single<List<SkuDetails>> = Single.create { emitter ->
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(type)
            .build()
        querySkuDetailsAsync(skuDetailsParams) { result: BillingResult, skuDetailsList: List<SkuDetails>? ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null) {
                    emitter.onSuccess(skuDetailsList.orEmpty())
                } else {
                    val cause = NullPointerException("Sku details list is null")
                    emitter.onError(BillingClientException(cause))
                }
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

internal fun BillingClient.queryPurchasesSingle(@BillingClient.SkuType type: String): Single<PurchasesResult> {
    return coroutineToSingle { queryPurchasesAsync(type) }
}

internal fun BillingClient.queryPurchaseHistorySingle(@BillingClient.SkuType type: String): Single<PurchaseHistoryResult> {
    return coroutineToSingle {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(type)
            .build()
        queryPurchaseHistory(params)
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