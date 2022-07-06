package com.frolo.core.ui.systembars

import android.app.Activity
import android.view.Window
import androidx.fragment.app.Fragment
import com.frolo.core.ui.R


val Window.defaultSystemBarsHost: SystemBarsHost? get() {
    val decorView = this.peekDecorView() ?: return null
    val tagKey: Int = R.id.default_system_bars_host
    var host = decorView.getTag(tagKey) as? SystemBarsHost
    if (host == null) {
        host = SystemBarsHostImpl(this)
        decorView.setTag(tagKey, host)
    }
    return host
}

val Activity.defaultSystemBarsHost: SystemBarsHost? get() = window?.defaultSystemBarsHost

val Fragment.defaultSystemBarsHost: SystemBarsHost? get() = activity?.defaultSystemBarsHost