package com.frolo.core.ui.fragment

import android.view.WindowInsets


fun interface WithCustomWindowInsets {
    fun onApplyWindowInsets(insets: WindowInsets): WindowInsets
}