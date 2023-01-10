package com.frolo.billing.playstore

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.GuardedBy
import androidx.annotation.UiThread
import com.android.billingclient.api.*
import com.frolo.billing.*
import com.frolo.billing.ProductDetails
import com.frolo.billing.PurchaseHistoryRecord
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean


@UiThread
class PlayStoreBillingManagerImpl(
    private val foregroundActivityWatcher: ForegroundActivityWatcher,
    private val isDebug: Boolean
): BillingManager {

    private val context: Context get() = foregroundActivityWatcher.context

    private val logger: Logger by lazy { Logger.create(LOG_TAG, isDebug) }

    /**
     * SharedPreferences for storing purchase details.
     */
    private val purchasesPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PURCHASES_PREFS_STORAGE_NAME, Context.MODE_PRIVATE)
    }

    private val client: BillingClient by lazy {
        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            logger.d("Purchases updated: result=$billingResult, purchases=$purchases")
            notifyPurchasesUpdated(billingResult, purchases)
            purchases?.also(::handlePurchases)
        }
        BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
    }

    // Last billing flow state
    private val billingFlowStateLock = Any()
    @GuardedBy("billingFlowStateLock")
    private var lastBillingFlowState: BillingFlowState? = null

    // Disposables
    private val internalDisposables = CompositeDisposable()

    // Schedulers
    private val mainThreadScheduler: Scheduler by lazy { AndroidSchedulers.mainThread() }
    private val queryScheduler: Scheduler by lazy { Schedulers.io() }
    private val computationScheduler: Scheduler by lazy { Schedulers.computation() }

    private val isPreparingBillingClient = AtomicBoolean(false)
    private val preparedBillingClientProcessor = PublishProcessor.create<Result<BillingClient>>()

    /**
     * Prepares the billing client: start the connection to make the client ready.
     * Only one preparation is performed at a time.
     */
    @UiThread
    private fun prepareBillingClient() {
        ThreadUtils.assertMainThread()

        if (isPreparingBillingClient.getAndSet(true)) {
            return
        }

        logger.d("Starting billing client connection")
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                logger.d("Billing setup finished: result=$result")
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    preparedBillingClientProcessor.onNext(Result.success(client))
                } else {
                    val err = BillingClientException(result)
                    preparedBillingClientProcessor.onNext(Result.failure(err))
                }
                isPreparingBillingClient.set(false)
            }

            override fun onBillingServiceDisconnected() {
                logger.d("Billing service disconnected")
                val err = NullPointerException("Billing service disconnected")
                preparedBillingClientProcessor.onNext(Result.failure(err))
                isPreparingBillingClient.set(false)
            }
        })
    }

    @UiThread
    private fun requirePreparedClient(): Single<BillingClient> {
        ThreadUtils.assertMainThread()

        if (client.isReady) {
            return Single.just(client)
        }

        prepareBillingClient()

        return preparedBillingClientProcessor
            .firstOrError()
            .map { result -> result.getOrThrow() }
    }

    private fun notifyPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        synchronized(billingFlowStateLock) {
            lastBillingFlowState?.also { state ->
                val billingFlowResult = getBillingFlowResult(result, state.productId)
                state.resultProcessor.onNext(billingFlowResult)
                state.resultProcessor.onComplete()
            }
            lastBillingFlowState = null
        }
    }

    private fun awaitBillingFlowResult(productId: ProductId): Single<BillingFlowResult> {
        return synchronized(billingFlowStateLock) {
            lastBillingFlowState?.also { state ->
                val billingFlowResult = BillingFlowFailure(
                    productId = productId,
                    cause = BillingFlowFailure.Cause.UNKNOWN,
                    exception = null
                )
                state.resultProcessor.onNext(billingFlowResult)
                state.resultProcessor.onComplete()
            }
            val processor = PublishProcessor.create<BillingFlowResult>().toSerialized()
            lastBillingFlowState = BillingFlowState(
                productId = productId,
                resultProcessor = processor
            )
            processor.firstOrError()
        }
    }

    /**
     * Handles purchases: stores their state in the local storage and acknowledges them.
     */
    private fun handlePurchases(purchases: List<Purchase>) {
        // Store the state of each purchase
        val editor: SharedPreferences.Editor = purchasesPreferences.edit()
        editor.clear()
        purchases.forEach { purchase ->
            val key = getPurchaseDetailsKey(purchase.skus)
            val details = PurchaseDetails.from(purchase)
            val value = PurchaseDetails.serializeToJson(details)
            editor.putString(key, value)
        }
        editor.apply()

        // Acknowledge each purchase, if needed
        val ackSources = purchases.mapNotNull { purchase ->
            if (purchase.isAcknowledged) {
                logger.d("Purchase is already acknowledged: skus=${purchase.skus}")
                return@mapNotNull null
            }

            val source = Completable.create { emitter ->
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val listener = AcknowledgePurchaseResponseListener { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        logger.d("Purchase has been acknowledged: skus=${purchase.skus}")
                        emitter.onComplete()
                    } else {
                        logger.d("Failed to acknowledge purchase: skus=${purchase.skus}")
                        emitter.onError(BillingClientException(result))
                    }
                }
                client.acknowledgePurchase(acknowledgePurchaseParams, listener)
            }

            source.subscribeOn(queryScheduler)
        }

        Completable.mergeDelayError(ackSources)
            .observeOn(mainThreadScheduler)
            .subscribe({ /* no-op */ }, { err -> logger.e(err) })
            .let(internalDisposables::add)
    }

    override fun sync(): Completable {
        val source = Completable.fromAction {
            if (!client.isReady) {
                prepareBillingClient()
            }
        }

        return source
            .subscribeOn(mainThreadScheduler)
            .observeOn(mainThreadScheduler)
    }

    override fun getProductDetails(productId: ProductId): Single<ProductDetails> {
        return requirePreparedClient().flatMap { billingClient ->
            billingClient.querySkuDetailsSingle(listOf(productId.sku), productId.billingSkyType)
                .observeOn(computationScheduler)
                .map { skuDetailsList ->
                    skuDetailsList.find { it.sku == productId.sku && it.type == productId.billingSkyType }
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

    override fun isProductPurchased(productId: ProductId, forceCheckFromApi: Boolean): Flowable<Boolean> {
        val key = getPurchaseDetailsKey(productId.skus)
        val localPurchaseDetails = RxPreference.ofString(purchasesPreferences, key).get()
        val checkedFromApiRef = AtomicBoolean(false)
        return localPurchaseDetails.observeOn(mainThreadScheduler).switchMapSingle { optionalDetailsJson ->

            if (optionalDetailsJson.isPresent && (checkedFromApiRef.get() || !forceCheckFromApi)) {
                val json = optionalDetailsJson.get()
                try {
                    val details = PurchaseDetails.deserializeFromJson(json)
                    val isPurchased = (details.state == Purchase.PurchaseState.PURCHASED)
                    logger.d("The local purchase details of ${productId.sku} is present: $details")
                    // The local state is present and we're not forced to check it from the API
                    return@switchMapSingle Single.just(isPurchased)
                } catch (err: Throwable) {
                    logger.e("Failed to deserialize purchase details: json=$json", err)
                }
            }

            logger.d("Checking purchase state of ${productId.sku} from API")
            requirePreparedClient().flatMap { billingClient ->
                billingClient.queryPurchasesSingle(productId.billingSkyType)
                    .doOnSuccess { result ->
                        checkedFromApiRef.set(true)
                        result.purchasesList.also(::handlePurchases)
                    }
                    .map { result ->
                        val desiredPurchase = result.purchasesList.find { productId.hasTheSameSkus(it.skus) }
                        desiredPurchase != null && (desiredPurchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                    }
                    .doOnSuccess { isPurchased ->
                        logger.d("Checked purchase state of ${productId.sku}: purchased=$isPurchased")
                    }
            }
        }
    }

    private fun launchBillingFlowImpl(productId: ProductId): Single<BillingResult> {
        return requirePreparedClient().observeOn(mainThreadScheduler).flatMap { billingClient ->
            billingClient.querySkuDetailsSingle(listOf(productId.sku), productId.billingSkyType).flatMap { skuDetailsList ->
                val skuDetails = skuDetailsList.find { skuDetails -> skuDetails.sku == productId.sku }
                        ?: throw NullPointerException("Could not find SKU details for sku=${productId.sku}")
                val params = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()

                val source: Single<BillingResult> = Single.fromCallable {
                    val activity = foregroundActivityWatcher.foregroundActivity
                            ?: throw NullPointerException("Could not find foreground activity")
                    billingClient.launchBillingFlow(activity, params)
                }

                source.subscribeOn(mainThreadScheduler)
            }
        }
    }

    override fun launchBillingFlow(productId: ProductId): Completable {
        return launchBillingFlowImpl(productId).ignoreElement()
    }

    override fun launchBillingFlowForResult(productId: ProductId): Single<BillingFlowResult> {
        return launchBillingFlowImpl(productId)
            .flatMap { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Launched OK, waiting for the result
                    awaitBillingFlowResult(productId)
                } else {
                    // Launching failed
                    val billingFlowResult = getBillingFlowResult(billingResult, productId)
                    Single.just(billingFlowResult)
                }
            }
    }

    override fun consumeProduct(productId: ProductId): Completable {
        return requirePreparedClient().observeOn(mainThreadScheduler).flatMapCompletable { billingClient ->
            billingClient.queryPurchasesSingle(productId.billingSkyType)
                .observeOn(computationScheduler)
                .map { purchasesResult ->
                    if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val purchase = purchasesResult.purchasesList.find { productId.hasTheSameSkus(it.skus) }
                        Optional.of(purchase)
                    } else {
                        val msg = "Failed to query purchases: responseCode=${purchasesResult.billingResult.responseCode}"
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

    override fun getPurchaseHistory(skuType: SkuType): Single<List<PurchaseHistoryRecord>> {
        return requirePreparedClient().observeOn(mainThreadScheduler).flatMap { billingClient ->
            billingClient.queryPurchaseHistorySingle(skuType.billingSkyType)
                .observeOn(computationScheduler)
                .map { result ->
                    result.purchaseHistoryRecordList.orEmpty().map { record ->
                        PurchaseHistoryRecord(
                            skus = record.skus.toList(),
                            purchaseTime = record.purchaseTime,
                            purchaseToken = record.purchaseToken,
                            signature = record.signature,
                            quantity = record.quantity
                        )
                    }
                }
                .observeOn(mainThreadScheduler)
        }
    }

    private data class BillingFlowState(
        val productId: ProductId,
        val resultProcessor: FlowableProcessor<BillingFlowResult>
    )

    companion object {
        private const val LOG_TAG = "BillingManager"

        private const val PURCHASES_PREFS_STORAGE_NAME = "com.frolo.billing.playstore.Purchases"

        private const val KEY_PURCHASE_DETAILS = "purchase_details"

        private fun getPurchaseDetailsKey(skus: List<String>): String {
            val sortedSkus = skus.sorted()
            val skusToken = sortedSkus.joinToString(separator = "_")
            return KEY_PURCHASE_DETAILS + "_" + skusToken
        }
    }
}