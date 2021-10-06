@file:Suppress("FunctionName")

package com.frolo.billing.playstore

import com.android.billingclient.api.BillingResult


class BillingClientException: RuntimeException {
    val billingResult: BillingResult?

    constructor(result: BillingResult): super(result.debugMessage) {
        billingResult = result
    }

    constructor(cause: Throwable?): super(cause) {
        billingResult = null
    }

    constructor(message: String?): super(message) {
        billingResult = null
    }
}

//val BillingResult.isServiceTimeout: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.SERVICE_TIMEOUT
//
//val BillingResult.isFeatureNotSupported: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED
//
//val BillingResult.isServiceDisconnected: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
//
//val BillingResult.isOK: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.OK
//
//val BillingResult.isServiceUnavailable: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
//
//val BillingResult.isBillingUnavailable: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
//
//val BillingResult.isItemUnavailable: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE
//
//val BillingResult.isDeveloperError: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR
//
//val BillingResult.isError: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.ERROR
//
//val BillingResult.isItemAlreadyOwned: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
//
//val BillingResult.isItemNotOwned: Boolean
//    get() = responseCode == BillingClient.BillingResponseCode.ITEM_NOT_OWNED

