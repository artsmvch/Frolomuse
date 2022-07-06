package com.frolo.core.ui.fragment

import androidx.annotation.ColorInt

interface WithCustomStatusBar {
    val isStatusBarVisible: Boolean

    @get:ColorInt
    val statusBarColor: Int
    val isStatusBarAppearanceLight: Boolean
}