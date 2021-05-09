package com.frolo.muse.billing

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.UiThread
import com.android.billingclient.api.*
import com.frolo.muse.BuildConfig
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.Logger
import com.frolo.muse.OptionalCompat
import com.frolo.muse.rx.subscribeSafely
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean


@UiThread
class BillingManager(private val frolomuseApp: FrolomuseApp) {

    private val context: Context get() = frolomuseApp

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_STORAGE_NAME, Context.MODE_PRIVATE)
    }

    private val client: BillingClient by lazy {
        val purchasesUpdatedListener = PurchasesUpdatedListener { _, purchases ->
            Logger.d(LOG_TAG, "Purchases updated: $purchases")
            purchases?.also(::handlePurchases)
        }
        BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
    }

    private val mainThreadScheduler: Scheduler by lazy { AndroidSchedulers.mainThread() }
    private val queryScheduler: Scheduler by lazy { Schedulers.io() }
    private val computationScheduler: Scheduler by lazy { Schedulers.computation() }

    private val disposables = CompositeDisposable()

    private val isPreparingBillingClient = AtomicBoolean(false)
    private val preparedBillingClientProcessor = PublishProcessor.create<Result<BillingClient>>()

    init {
        if (DEBUG) {
            Logger.d(LOG_TAG, "Storage: ${preferences.all}")
        }
    }

    /**
     * Prepares the billing client: start the connection to make the client ready.
     * Only one preparation is performed at a time.
     */
    @UiThread
    private fun prepareBillingClient() {
        if (isPreparingBillingClient.getAndSet(true)) {
            return
        }

        Logger.d(LOG_TAG, "Starting billing client connection")
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                Logger.d(LOG_TAG, "Billing setup finished: result=$result")
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    preparedBillingClientProcessor.onNext(Result.success(client))
                } else {
                    val err = BillingClientException(result)
                    preparedBillingClientProcessor.onNext(Result.failure(err))
                }
                isPreparingBillingClient.set(false)
            }

            override fun onBillingServiceDisconnected() {
                Logger.d(LOG_TAG, "Billing service disconnected")
                val err = NullPointerException("Billing service disconnected")
                preparedBillingClientProcessor.onNext(Result.failure(err))
                isPreparingBillingClient.set(false)
            }
        })
    }

    @UiThread
    private fun requirePreparedClient(): Single<BillingClient> {
        if (client.isReady) {
            return Single.just(client)
        }

        prepareBillingClient()

        return preparedBillingClientProcessor
            .firstOrError()
            .map { result -> result.getOrThrow() }
    }

    /**
     * Handles purchases: stores their state in the local storage and acknowledges them.
     */
    private fun handlePurchases(purchases: List<Purchase>) {
        // Store the state of each purchase
        val editor = preferences.edit()
        editor.clear()
        purchases.forEach { purchase ->
            val key = getPurchaseDetailsKey(purchase.sku)
            val details = PurchaseDetails.from(purchase)
            val value = PurchaseDetails.serializeToJson(details)
            editor.putString(key, value)
        }
        editor.apply()

        // Acknowledge each purchase, if needed
        val ackSources = purchases.mapNotNull { purchase ->
            if (purchase.isAcknowledged) {
                Logger.d(LOG_TAG, "Purchase is already acknowledged: sku=${purchase.sku}")
                return@mapNotNull null
            }

            val source = Completable.create { emitter ->
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val listener = AcknowledgePurchaseResponseListener { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Logger.d(LOG_TAG, "Purchase has been acknowledged: sku=${purchase.sku}")
                        emitter.onComplete()
                    } else {
                        Logger.d(LOG_TAG, "Failed to acknowledge purchase: sku=${purchase.sku}")
                        emitter.onError(BillingClientException(result))
                    }
                }
                client.acknowledgePurchase(acknowledgePurchaseParams, listener)
            }

            source.subscribeOn(queryScheduler)
        }

        Completable.mergeDelayError(ackSources)
            .observeOn(mainThreadScheduler)
            .subscribeSafely()
            .let(disposables::add)
    }

    fun sync() {
        if (!client.isReady) {
            prepareBillingClient()
        }
    }

    fun getProductDetails(productId: ProductId): Single<ProductDetails> {
        return requirePreparedClient().flatMap { billingClient ->
            billingClient.querySkuDetailsSingle(listOf(productId.sku), productId.type)
                .observeOn(computationScheduler)
                .map { skuDetailsList ->
                    skuDetailsList.find { it.sku == productId.sku && it.type == productId.type }
                }
                .map { skuDetails ->
                    ProductDetails(
                        productId = productId,
                        title = skuDetails.title,
                        description = skuDetails.description,
                        iconUrl = skuDetails.iconUrl,
                        price = skuDetails.price,
                        priceCurrencyCode = skuDetails.priceCurrencyCode
                    )
                }
                .observeOn(mainThreadScheduler)
        }
    }

    fun isProductPurchased(productId: ProductId, forceCheckFromApi: Boolean = true): Flowable<Boolean> {
        val key = getPurchaseDetailsKey(productId.sku)
        val localPurchaseDetails = RxPreference.ofString(preferences, key).get()
        val checkedFromApiRef = AtomicBoolean(false)
        return localPurchaseDetails.observeOn(mainThreadScheduler).switchMapSingle { optionalDetailsJson ->

            if (optionalDetailsJson.isPresent && (checkedFromApiRef.get() || !forceCheckFromApi)) {
                val json = optionalDetailsJson.get()
                try {
                    val details = PurchaseDetails.deserializeFromJson(json)
                    val isPurchased = (details.state == Purchase.PurchaseState.PURCHASED)
                    Logger.d(LOG_TAG, "The local purchase details of ${productId.sku} is present: $details")
                    // The local state is present and we're not forced to check it from the API
                    return@switchMapSingle Single.just(isPurchased)
                } catch (err: Throwable) {
                    Logger.e(LOG_TAG, "Failed to deserialize purchase details: json=$json", err)
                }
            }

            Logger.d(LOG_TAG, "Checking purchase state of ${productId.sku} from API")
            requirePreparedClient().flatMap { billingClient ->
                billingClient.queryPurchasesSingle(productId.type)
                    .doOnSuccess { result ->
                        checkedFromApiRef.set(true)
                        result.purchasesList?.also(::handlePurchases)
                    }
                    .map { result ->
                        val desiredPurchase = result.purchasesList?.find { it.sku == productId.sku }
                        desiredPurchase != null && (desiredPurchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                    }
                    .doOnSuccess { isPurchased ->
                        Logger.d(LOG_TAG, "Checked purchase state of ${productId.sku}: purchased=$isPurchased")
                    }
            }
        }
    }

    fun launchBillingFlow(productId: ProductId): Single<BillingResult> {
        return requirePreparedClient().observeOn(mainThreadScheduler).flatMap { billingClient ->
            billingClient.querySkuDetailsSingle(listOf(productId.sku), productId.type).flatMap { skuDetailsList ->
                val skuDetails = skuDetailsList.find { skuDetails -> skuDetails.sku == productId.sku }
                        ?: throw NullPointerException("Could not find SKU details for sku=${productId.sku}")
                val params = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
                val activity = frolomuseApp.foregroundActivity
                        ?: throw NullPointerException("Could not find foreground activity")
                Single.fromCallable { billingClient.launchBillingFlow(activity, params) }
            }
        }
    }

    fun consumeProduct(productId: ProductId): Completable {
        return requirePreparedClient().observeOn(mainThreadScheduler).flatMapCompletable { billingClient ->
            billingClient.queryPurchasesSingle(productId.type)
                .observeOn(computationScheduler)
                .map { purchasesResult ->
                    if (purchasesResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val purchase = purchasesResult.purchasesList?.find { it.sku == productId.sku }
                        OptionalCompat.of(purchase)
                    } else {
                        val msg = "Failed to query purchases: responseCode=${purchasesResult.responseCode}"
                        throw IllegalStateException(msg)
                    }
                }
                .observeOn(mainThreadScheduler)
                .flatMapCompletable purchasesResult@ { optionalPurchase ->
                    val purchase = optionalPurchase.value
                        // If the purchase is null, then we consider it refunded
                        ?: return@purchasesResult Completable.complete()

                    Completable.create { emitter ->
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        val listener = ConsumeResponseListener { result, _ ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                emitter.onComplete()
                            } else {
                                emitter.onError(BillingClientException(result))
                            }
                        }
                        billingClient.consumeAsync(consumeParams, listener)
                    }
                }
        }
    }

    companion object {

        private val DEBUG = BuildConfig.DEBUG

        private const val LOG_TAG = "BillingManager"

        private const val PREFS_STORAGE_NAME = "com.frolo.muse.billing.BillingStorage"

        private const val KEY_PURCHASE_DETAILS = "purchase_details"

        private fun getPurchaseDetailsKey(sku: String): String {
            return KEY_PURCHASE_DETAILS + "_" + sku
        }
    }
}