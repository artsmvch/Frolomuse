package com.frolo.core.ui.toast

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.frolo.ui.ActivityUtils

val FragmentActivity.toastManager: ToastManager? get() {
    if (ActivityUtils.isFinishingOrDestroyed(this)) {
        return null
    }
    // TODO: cache the toast manager
    return SimpleToastManager(this)
}

val Fragment.toastManager: ToastManager? get() {
    return activity?.toastManager
}