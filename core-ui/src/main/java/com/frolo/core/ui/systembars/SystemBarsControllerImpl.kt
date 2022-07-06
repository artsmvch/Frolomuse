package com.frolo.core.ui.systembars

import android.view.Window
import androidx.annotation.ColorInt
import com.frolo.ui.SystemBarUtils


internal class SystemBarsControllerImpl(
    private val getWindowLambda: () -> Window?
): SystemBarsController {

    private inline fun applyToWindow(block: Window.() -> Unit) {
        val safeWindow = getWindowLambda.invoke()
            ?: return
        safeWindow.block()
    }

    override fun setStatusBarVisible(isVisible: Boolean) {
        applyToWindow {
            SystemBarUtils.setStatusBarVisible(this, isVisible)
        }
    }

    override fun setStatusBarColor(@ColorInt color: Int) {
        applyToWindow {
            SystemBarUtils.setStatusBarColor(this, color)
        }
    }

    override fun setStatusBarAppearanceLight(isLight: Boolean) {
        applyToWindow {
            SystemBarUtils.setStatusBarAppearanceLight(this, isLight)
        }
    }
}