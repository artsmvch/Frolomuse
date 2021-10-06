package com.frolo.muse.billing

import com.frolo.billing.ProductId
import com.frolo.billing.SkuType

object Products {
    /**
     * Simple premium that removes ads and unlocks some customization options.
     */
    val PREMIUM = ProductId("premium", SkuType.IN_APP)

    /**
     * Unlocks additional themes.
     */
    val PALETTE = ProductId("palette", SkuType.IN_APP)
}