package com.frolo.billing

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

abstract class BillingManagerWrapper(
    private val wrapped: BillingManager
) : BillingManager {

    constructor(): this(BillingManagerStub())

    override fun sync(): Completable = wrapped.sync()

    override fun getProductDetails(productId: ProductId): Single<ProductDetails> =
        wrapped.getProductDetails(productId)

    override fun isProductPurchased(
        productId: ProductId,
        forceCheckFromApi: Boolean
    ): Flowable<Boolean> = wrapped.isProductPurchased(productId, forceCheckFromApi)

    override fun launchBillingFlow(productId: ProductId): Completable =
        wrapped.launchBillingFlow(productId)

    override fun launchBillingFlowForResult(productId: ProductId): Single<BillingFlowResult> =
        wrapped.launchBillingFlowForResult(productId)

    override fun consumeProduct(productId: ProductId): Completable =
        wrapped.consumeProduct(productId)

}