package com.frolo.ui

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
}