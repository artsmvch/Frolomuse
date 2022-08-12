package com.frolo.muse.interactor.billing

import com.frolo.billing.BillingManager
import com.frolo.billing.ProductDetails
import com.frolo.billing.ProductId
import com.frolo.muse.billing.TrialManager
import com.frolo.muse.billing.TrialStatus
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class PremiumManager constructor(
    private val billingManager: BillingManager,
    private val trialManager: TrialManager
) {

    fun sync(): Completable {
        val sources = listOf(
            billingManager.sync(),
            trialManager.sync()
        )
        return Completable.merge(sources)
    }

    fun getProductDetails(productId: ProductId): Single<ProductDetails> =
        billingManager.getProductDetails(productId)

    fun isProductPurchased(
        productId: ProductId,
        forceCheckFromApi: Boolean = true
    ): Flowable<Boolean> = billingManager.isProductPurchased(productId, forceCheckFromApi)

    fun launchBillingFlow(productId: ProductId): Completable =
        billingManager.launchBillingFlow(productId)

    fun consumeProduct(productId: ProductId): Completable =
        billingManager.consumeProduct(productId)

    fun getTrialStatus(): Flowable<TrialStatus> = trialManager.getTrialStatus()

    fun activateTrialVersion(): Completable = trialManager.activateTrialVersion()

    fun resetTrial(): Completable = trialManager.resetTrial()

}