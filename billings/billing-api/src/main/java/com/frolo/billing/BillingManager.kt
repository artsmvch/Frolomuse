package com.frolo.billing

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


interface BillingManager {

    /**
     * Synchronizes state.
     */
    fun sync(): Completable

    /**
     * Returns product details for the given [productId].
     */
    fun getProductDetails(productId: ProductId): Single<ProductDetails>

    /**
     * Checks if the product with [productId] is purchased and active. A purchased product
     * can be consumed later using [consumeProduct] method.
     *
     * [forceCheckFromApi] determines whether the check should go through the remote api.
     * NOTE: passing [forceCheckFromApi] may not be an optimal solution,
     * but guarantees the actual purchase status.
     */
    fun isProductPurchased(productId: ProductId, forceCheckFromApi: Boolean = true): Flowable<Boolean>

    /**
     * Launches the purchase process for a product with [productId].
     */
    fun launchBillingFlow(productId: ProductId): Completable

    /**
     * Consumes all purchased products with the given [productId].
     */
    fun consumeProduct(productId: ProductId): Completable
}