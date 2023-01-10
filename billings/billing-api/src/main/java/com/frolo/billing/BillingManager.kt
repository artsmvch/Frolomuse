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
     * NOTE: it fires the completion callback when the flow is launched,
     * rather than completed / terminated.
     */
    fun launchBillingFlow(productId: ProductId): Completable

    /**
     * Launches the purchase process for a product with [productId] and waits for the result,
     * that is, fires [BillingFlowSuccess] when the product is purchased,
     * and fires [BillingFlowFailure] if the user cancels the flow,
     * or the billing service is unavailable, or another kind of error occurs.
     */
    fun launchBillingFlowForResult(productId: ProductId): Single<BillingFlowResult>

    /**
     * Consumes all purchased products with the given [productId].
     */
    fun consumeProduct(productId: ProductId): Completable

    /**
     * Returns the purchase history for the given [skuType].
     */
    fun getPurchaseHistory(skuType: SkuType): Single<List<PurchaseHistoryRecord>>
}