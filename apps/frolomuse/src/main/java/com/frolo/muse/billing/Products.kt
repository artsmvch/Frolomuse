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

    // Donations
    val DONATE_THANKS = ProductId("donate_thanks", SkuType.IN_APP)
    val DONATE_COFFEE = ProductId("donate_coffee", SkuType.IN_APP)
    val DONATE_MOVIE_TICKET = ProductId("donate_movie_ticket", SkuType.IN_APP)
    val DONATE_PIZZA = ProductId("donate_pizza", SkuType.IN_APP)
    val DONATE_MEAL = ProductId("donate_meal", SkuType.IN_APP)
    val DONATE_GYM_MEMBERSHIP = ProductId("donate_gym_membership", SkuType.IN_APP)
}