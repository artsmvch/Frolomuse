package com.frolo.ui

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

/**
 * Extension for [ColorUtils].
 */
object ColorUtils2 {
    @ColorInt
    fun setAlphaComponentFloat(@ColorInt color: Int, alphaF: Float): Int {
        val alphaInt: Int = (255 * alphaF).toInt().coerceIn(0, 255)
        return ColorUtils.setAlphaComponent(color, alphaInt)
    }

    @ColorInt
    fun multiplyAlphaComponent(@ColorInt color: Int, factor: Float): Int {
        val currentAlphaF = (Color.alpha(color) / 255f)
        val targetAlphaF = (currentAlphaF * factor).coerceIn(0f, 1f)
        return setAlphaComponentFloat(color, targetAlphaF)
    }

    @ColorInt
    fun makeOpaque(@ColorInt color: Int): Int {
        return ColorUtils.setAlphaComponent(color, 255)
    }
}