package com.frolo.muse.ui.main

import android.view.View


internal object WindowInsetsHelper {
    private val SKIPPED_WINDOW_INSETS_LISTENER = View.OnApplyWindowInsetsListener { _, insets ->
        insets
    }

    /**
     * Causes [view] to skip processing and changing window insets.
     * Window insets will just be dispatched down the hierarchy.
     */
    fun skipWindowInsets(view: View) {
        setupWindowInsets(view, SKIPPED_WINDOW_INSETS_LISTENER)
    }

    fun setupWindowInsets(view: View, listener: View.OnApplyWindowInsetsListener) {
        view.fitsSystemWindows = true
        view.setOnApplyWindowInsetsListener(listener)
        view.requestApplyInsets()
    }
}