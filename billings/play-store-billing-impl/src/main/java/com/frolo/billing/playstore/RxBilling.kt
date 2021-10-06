package com.frolo.billing.playstore

import android.content.Context
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers


private fun getMainThreadScheduler(): Scheduler {
    return AndroidSchedulers.mainThread()
}

private fun getQueryScheduler(): Scheduler {
    return Schedulers.io()
}

/**
 * Returns an instance of [BillingClient] that is connected to the billing service and is ready for work.
 */
fun prepareBillingClient(
    context: Context,
    purchasesUpdatedListener: PurchasesUpdatedListener
): Single<BillingClient> {
    val singleSource: Single<BillingClient> = Single.create { emitter ->

        val client: BillingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()

        emitter.setDisposable(Disposables.fromAction {
            //client.endConnection()
        })

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    emitter.onSuccess(client)
                } else {
                    emitter.onError(BillingClientException(result))
                }
            }

            override fun onBillingServiceDisconnected() {
                emitter.onError(BillingClientException("Service was disconnected"))
            }
        })
    }

    val scheduler: Scheduler = getMainThreadScheduler()

    return singleSource
        .subscribeOn(scheduler)
        .unsubscribeOn(scheduler)
        .observeOn(scheduler)
}

/**
 * Returns an instance of [BillingClient] that is connected to the billing service and is ready for work.
 */
fun prepareBillingClient(
    context: Context,
    onPurchasesUpdated: (result: BillingResult, purchases: List<Purchase>?) -> Unit
): Single<BillingClient> = prepareBillingClient(context, PurchasesUpdatedListener(onPurchasesUpdated))

fun BillingClient.querySkuDetailsSingle(skuList: List<String>, @BillingClient.SkuType type: String): Single<List<SkuDetails>> {
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

fun BillingClient.queryPurchasesSingle(@BillingClient.SkuType type: String): Single<Purchase.PurchasesResult> {
    return Single.fromCallable { queryPurchases(type) }
        .subscribeOn(getQueryScheduler())
        .unsubscribeOn(getMainThreadScheduler())
        .observeOn(getMainThreadScheduler())
}

fun BillingClient.launchBillingFlowCompletable(): Completable {
    return Completable.error(UnsupportedOperationException())
}