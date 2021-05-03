package com.frolo.muse.billing

import com.android.billingclient.api.BillingClient


enum class ProductId(val sku: String, @BillingClient.SkuType val type: String) {
    /**
     * Simple premium that removes ads and unlocks some customization options.
     */
    PREMIUM("premium", BillingClient.SkuType.INAPP),

    /**
     * Unlocks additional themes.
     */
    PALETTE("palette", BillingClient.SkuType.INAPP)
}