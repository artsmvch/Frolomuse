package com.frolo.core.ui.systembars

import androidx.annotation.ColorInt


interface SystemBarsController {
    fun setStatusBarVisible(isVisible: Boolean)
    fun setStatusBarColor(@ColorInt color: Int)
    fun setStatusBarAppearanceLight(isLight: Boolean)
}