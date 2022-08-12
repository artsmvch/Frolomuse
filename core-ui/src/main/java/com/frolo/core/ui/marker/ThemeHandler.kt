package com.frolo.core.ui.marker

/**
 * UI components that implement this interface should be able to handle themes changes.
 */
fun interface ThemeHandler {
    /**
     * Called when the app's theme has been changed.
     */
    fun handleThemeChange()
}