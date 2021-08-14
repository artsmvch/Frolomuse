package com.frolo.muse.billing

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.UiThread
import androidx.core.content.edit
import com.android.billingclient.api.*
import com.frolo.muse.BuildConfig
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.Logger
import com.frolo.muse.OptionalCompat
import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


@UiThread
class BillingManager(private val frolomuseApp: FrolomuseApp) {

    private val context: Context get() = frolomuseApp

    /**
     * SharedPreferences for storing purchase details.
     */
    private val purchasesPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PURCHASES_PREFS_STORAGE_NAME, Context.MODE_PRIVATE)
    }

    /**
     * SharedPreferences for storing trial information.
     */
    private val trialPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(TRIAL_PREFS_STORAGE_NAME, Context.MODE_PRIVATE)
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
        val editor: SharedPreferences.Editor = purchasesPreferences.edit()
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

        if (DEBUG) {
            Logger.d(LOG_TAG, "PurchasesPreferences: ${purchasesPreferences.all}")
            Logger.d(LOG_TAG, "TrialPreferences: ${trialPreferences.all}")
        }

        if (!client.isReady) {
            prepareBillingClient()
        }

        // The trial period is considered valid if it is >= 0L
        val isTrialDurationValid: Boolean = try {
            trialPreferences.getLong(KEY_TRIAL_DURATION_MILLIS, -1L) >= 0L
        } catch (ignored: Throwable) {
            false
        }
        if (!isTrialDurationValid) {
            FirebaseRemoteConfigUtil.fetchAndActivate()
                .map { config ->
                    // Should throw an exception if the value is empty or invalid
                    config.getValue(REMOTE_KEY_TRIAL_DURATION_MILLIS).asString().toLong()
                }
                .doOnSuccess { duration ->
                    trialPreferences.edit { putLong(KEY_TRIAL_DURATION_MILLIS, duration) }
                }
                .doOnError { err ->
                    Logger.e(LOG_TAG, "Failed to sync Trial duration millis", err)
                }
                .subscribeSafely()
        }
    }

    //region Trial
    fun getTrialStatus(): Flowable<TrialStatus> {
        return RxPreference.ofLong(trialPreferences, KEY_TRIAL_ACTIVATION_TIME_MILLIS)
            .get()
            .map { activationTimeOptional ->

                val trialDurationMillis: Long = if (DEBUG) {
                    // For debugging, trial duration is only 5 minutes
                    DEBUG_TRIAL_DURATION_MILLIS
                } else {
                    trialPreferences.getLong(KEY_TRIAL_DURATION_MILLIS, DEFAULT_TRIAL_DURATION_MILLIS)
                }

                when {
                    trialDurationMillis <= 0 -> {
                        // The duration is invalid or equal to 0 => no trial available.
                        TrialStatus.NotAvailable
                    }
                    activationTimeOptional.isPresent -> {
                        // There is an activation time present, we need to check if it has expired
                        val currentTimeMillis: Long = getCurrentTimeMillis()
                        val activationTimeMillis: Long = activationTimeOptional.get()
                        val expirationTimeMillis: Long = activationTimeMillis + trialDurationMillis
                        if (currentTimeMillis < expirationTimeMillis) TrialStatus.Activated else TrialStatus.Expired
                    }
                    else -> {
                        // There is no activation time in the preferences.
                        // We consider the trial available (not yet activated).
                        TrialStatus.Available(trialDurationMillis)
                    }
                }
            }
            .observeOn(mainThreadScheduler)
    }

    fun activateTrialVersion(): Completable {
        val source = Completable.fromAction {
            if (trialPreferences.contains(KEY_TRIAL_ACTIVATION_TIME_MILLIS)) {
                throw IllegalStateException("The trial is already activated")
            }
            val currentTimeMillis: Long = getCurrentTimeMillis()
            trialPreferences.edit { putLong(KEY_TRIAL_ACTIVATION_TIME_MILLIS, currentTimeMillis) }
        }

        return source.subscribeOn(queryScheduler).observeOn(mainThreadScheduler)
    }

    /**
     * Resets trial activation. NOTE: should be used only for testing.
     */
    fun resetTrial(): Completable {
        val source = Completable.fromAction {
            trialPreferences.edit {
                remove(KEY_TRIAL_ACTIVATION_TIME_MILLIS)
            }
        }

        return source.subscribeOn(queryScheduler).observeOn(mainThreadScheduler)
    }
    //endregion

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
        val localPurchaseDetails = RxPreference.ofString(purchasesPreferences, key).get()
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

                val source: Single<BillingResult> = Single.fromCallable {
                    val activity = frolomuseApp.foregroundActivity
                            ?: throw NullPointerException("Could not find foreground activity")
                    billingClient.launchBillingFlow(activity, params)
                }

                source.subscribeOn(mainThreadScheduler)
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

        private const val PURCHASES_PREFS_STORAGE_NAME = "com.frolo.muse.billing.Purchases"
        private const val TRIAL_PREFS_STORAGE_NAME = "com.frolo.muse.billing.Trial"

        private const val REMOTE_KEY_TRIAL_DURATION_MILLIS = "trial_duration_millis"
        private const val KEY_TRIAL_DURATION_MILLIS = "trial_duration_millis"
        private const val KEY_TRIAL_ACTIVATION_TIME_MILLIS = "trial_activation_time_millis"
        private const val KEY_PURCHASE_DETAILS = "purchase_details"

        private val DEFAULT_TRIAL_DURATION_MILLIS = TimeUnit.DAYS.toMillis(3)
        private val DEBUG_TRIAL_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5)

        private fun getPurchaseDetailsKey(sku: String): String {
            return KEY_PURCHASE_DETAILS + "_" + sku
        }

        private fun getCurrentTimeMillis(): Long {
            return System.currentTimeMillis()
        }
    }
}