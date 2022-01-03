package com.frolo.billing

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

internal class BillingManagerStub : BillingManager {

    private fun notImplementedError(): Throwable {
        return UnsupportedOperationException("Not implemented")
    }

    override fun sync(): Completable {
        return Completable.error(notImplementedError())
    }

    override fun getProductDetails(productId: ProductId): Single<ProductDetails> {
        return Single.error(notImplementedError())
    }

    override fun isProductPurchased(
        productId: ProductId,
        forceCheckFromApi: Boolean
    ): Flowable<Boolean> {
        return Flowable.error(notImplementedError())
    }

    override fun launchBillingFlow(productId: ProductId): Completable {
        return Completable.error(notImplementedError())
    }

    override fun launchBillingFlowForResult(productId: ProductId): Single<BillingFlowResult> {
        return Single.error(notImplementedError())
    }

    override fun consumeProduct(productId: ProductId): Completable {
        return Completable.error(notImplementedError())
    }
}