package com.frolo.muse.ui.main

import android.view.View


internal object WindowInsetsHelper {
    /**
     * Causes [view] to skip processing and changing window insets.
     * Window insets will just be dispatched down the hierarchy.
     */
    fun skipWindowInsets(view: View) {
        setupWindowInsets(view) { _, insets -> insets }
    }

    fun setupWindowInsets(view: View, listener: View.OnApplyWindowInsetsListener) {
        view.fitsSystemWindows = true
        view.setOnApplyWindowInsetsListener(listener)
        view.requestApplyInsets()
    }
}