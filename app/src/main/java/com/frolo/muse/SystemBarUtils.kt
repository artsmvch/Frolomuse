package com.frolo.muse

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils


object SystemBarUtils {

    /**
     * Enables or disables the bit [flag] in the system UI visibility flags of the given [view].
     * Returns true if the system UI visibility flags have changed, false otherwise.
     */
    private fun setSystemUiVisibilityFlagOn(view: View, flag: Int, on: Boolean): Boolean {
        var resultFlags = view.systemUiVisibility

        if (on) {
            resultFlags = resultFlags or flag
        } else {
            resultFlags = resultFlags and flag.inv()
        }

        if (view.systemUiVisibility != resultFlags) {
            view.systemUiVisibility = resultFlags
            return true
        } else {
            return false
        }
    }

    /**
     * Returns true if the [color] is light.
     */
    fun isLight(@ColorInt color: Int): Boolean {
        val luminance = ColorUtils.calculateLuminance(color)
        return luminance > 0.75
    }

    /**
     * Returns true if the [color] is dark.
     */
    fun isDark(@ColorInt color: Int): Boolean {
        return !isLight(color)
    }

    fun setNavigationBarColor(window: Window, @ColorInt color: Int) {
        window.navigationBarColor = color
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setNavigationBarAppearanceLight(window: Window, isLight: Boolean)  {
        if (isLight) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setSystemUiVisibilityFlagOn(window.decorView, View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR, true)
        } else {
            setSystemUiVisibilityFlagOn(window.decorView, View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR, false)
        }
    }
}