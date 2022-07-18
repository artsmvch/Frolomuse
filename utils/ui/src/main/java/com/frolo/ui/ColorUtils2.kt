package com.frolo.ui

import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import kotlin.concurrent.getOrSet

/**
 * Extension for [ColorUtils].
 */
object ColorUtils2 {

    private val hsl: ThreadLocal<FloatArray> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ThreadLocal.withInitial { FloatArray(3) }
        } else {
            ThreadLocal<FloatArray>()
        }
    }

    private fun getHslTmp(): FloatArray {
        return hsl.getOrSet { FloatArray(3) }
    }

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

    fun isLight(@ColorInt color: Int): Boolean {
        return color != Color.TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5
    }

    @FloatRange(from = 0.0, to = 1.0)
    fun getLightness(@ColorInt color: Int): Float {
        val hsl = getHslTmp()
        ColorUtils.colorToHSL(color, hsl)
        return hsl[2]
    }

    @ColorInt
    fun setLightness(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) lightness: Float): Int {
        val hsl = getHslTmp()
        ColorUtils.colorToHSL(color, hsl)
        hsl[2] = lightness
        return ColorUtils.HSLToColor(hsl)
    }
}