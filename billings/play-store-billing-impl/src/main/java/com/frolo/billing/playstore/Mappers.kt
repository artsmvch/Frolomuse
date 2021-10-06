package com.frolo.billing.playstore

import com.android.billingclient.api.BillingClient
import com.frolo.billing.ProductId
import com.frolo.billing.SkuType

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