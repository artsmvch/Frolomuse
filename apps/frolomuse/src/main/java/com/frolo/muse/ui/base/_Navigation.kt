package com.frolo.muse.ui.base

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.frolo.debug.DebugUtils


fun Fragment.setupNavigation(toolbar: Toolbar) {
    val safeActivity = this.activity ?: kotlin.run {
        DebugUtils.dumpOnMainThread(IllegalStateException())
        return
    }

    val safeNavigator = safeActivity as? SimpleFragmentNavigator ?: kotlin.run {
        DebugUtils.dumpOnMainThread(IllegalArgumentException())
        return
    }

    toolbar.setNavigationOnClickListener {
        safeNavigator.pop()
    }
}