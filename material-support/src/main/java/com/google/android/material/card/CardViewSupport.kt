package com.google.android.material.card

import kotlin.math.cos


object CardViewSupport {

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

}