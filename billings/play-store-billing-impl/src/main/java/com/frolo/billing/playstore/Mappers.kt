package com.frolo.billing.playstore

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.frolo.billing.*

@BillingClient.SkuType
internal val SkuType.billingSkyType: String
    get() {
        return when (this) {
            SkuType.IN_APP -> BillingClient.SkuType.INAPP
            SkuType.SUBS -> BillingClient.SkuType.SUBS
        }
    }

@BillingClient.SkuType
internal val ProductId.billingSkyType: String
    get() {
        return type.billingSkyType
    }

internal fun getBillingFlowResult(result: BillingResult, productId: ProductId): BillingFlowResult {
    val responseCode = result.responseCode
    if (responseCode == BillingClient.BillingResponseCode.OK) {
        return BillingFlowSuccess(productId)
    } else {
        val cause = when (responseCode) {
            BillingClient.BillingResponseCode.USER_CANCELED ->
                BillingFlowFailure.Cause.USER_CANCELED

            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                BillingFlowFailure.Cause.UNAVAILABLE

            else -> BillingFlowFailure.Cause.UNKNOWN
        }
        return BillingFlowFailure(
            productId = productId,
            cause = cause,
            exception = null
        )
    }
}