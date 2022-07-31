package com.frolo.core.ui.fragment

import androidx.core.view.WindowInsetsCompat


fun interface WithCustomWindowInsets {
    fun onApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat
}