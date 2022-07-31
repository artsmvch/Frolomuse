package com.frolo.muse.ui.main

import android.util.Log
import android.view.View
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.frolo.muse.BuildInfo


internal object WindowInsetsHelper {
    private const val LOG_TAG = "WindowInsetsHelper"
    private val isDebug: Boolean get() = BuildInfo.isDebug()

    private val SKIPPED_WINDOW_INSETS_LISTENER = OnApplyWindowInsetsListener { view, insets ->
        if (isDebug) {
            Log.d(LOG_TAG, "Skip window insets for $view, " +
                    "insets=${toStringDetailed(insets)}")
        }
        insets
    }

    private fun toStringDetailed(insets: WindowInsetsCompat): String {
        val systemInsets = insets.let {
            "Insets{" +
                    "left=" + it.systemWindowInsetLeft +
                    ", top=" + it.systemWindowInsetTop +
                    ", right=" + it.systemWindowInsetRight +
                    ", bottom=" + it.systemWindowInsetBottom +
                    '}'
        }
        return "WindowInsets{system=${systemInsets}}}"
    }

    /**
     * Causes [view] to skip processing and changing window insets.
     * Window insets will just be dispatched down the hierarchy.
     */
    fun skipWindowInsets(view: View) {
        setupWindowInsets(view, SKIPPED_WINDOW_INSETS_LISTENER)
    }

    fun setupWindowInsets(view: View, listener: OnApplyWindowInsetsListener) {
        view.fitsSystemWindows = true
        ViewCompat.setOnApplyWindowInsetsListener(view, listener)
        view.requestApplyInsets()
    }
}