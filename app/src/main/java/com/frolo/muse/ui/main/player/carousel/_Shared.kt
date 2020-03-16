package com.frolo.muse.ui.main.player.carousel

import kotlin.math.cos


/**
 * CardView's compat horizontal padding is calculated in this way.
 */
fun calculateCardHorizontalShadowPadding(maxCardElevation: Float, cornerRadius: Float): Float {
    return (maxCardElevation + (1 - cos(Math.PI / 4)) * cornerRadius).toFloat()
}

/**
 * CardView's compat vertical padding is calculated in this way.
 */
fun calculateCardVerticalShadowPadding(maxCardElevation: Float, cornerRadius: Float): Float {
    return (maxCardElevation * 1.5 + (1 - cos(Math.PI / 4)) * cornerRadius).toFloat()
}