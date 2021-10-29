package com.google.android.material.card

import android.annotation.SuppressLint
import androidx.annotation.ColorInt
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

    @SuppressLint("RestrictedApi")
    fun setShadowColor(cardView: MaterialCardView, @ColorInt color: Int) {
        try {
            val cardViewHelperField = MaterialCardView::class.java.getDeclaredField("cardViewHelper")
            cardViewHelperField.isAccessible = true
            val cardViewHelper = cardViewHelperField.get(cardView) as MaterialCardViewHelper
            val background = cardViewHelper.background
            background.setShadowColor(color)
        } catch (ignored: Throwable) {
        }
    }

}