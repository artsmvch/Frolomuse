package com.frolo.muse.ui.base

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment


fun Fragment.setupNavigation(toolbar: Toolbar) {
    val navigator = requireActivity() as FragmentNavigator
    toolbar.setNavigationOnClickListener {
        navigator.pop()
    }
}