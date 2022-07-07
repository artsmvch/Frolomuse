package com.frolo.core.ui.systembars

import android.app.Activity
import android.view.Window
import androidx.fragment.app.Fragment
import com.frolo.core.ui.R


private val SYSTEM_BARS_HOST_KEY: Int = R.id.default_system_bars_host

val Window.defaultSystemBarsHost: SystemBarsHost? get() {
    val decorView = this.peekDecorView() ?: return null
    val host = decorView.getTag(SYSTEM_BARS_HOST_KEY) as? SystemBarsHostImpl
    // Check if the window matches. The window may be changed when the owner (e.g. activity)
    // has been recreated. In such a case, the decor view is retrieved from the preserved
    // window and set to the new one (spent a lot of time figuring this out).
    if (host != null && host.window == this) {
        return host
    }
    val newHost = SystemBarsHostImpl(this)
    decorView.setTag(SYSTEM_BARS_HOST_KEY, newHost)
    return newHost
}

val Activity.defaultSystemBarsHost: SystemBarsHost? get() = window?.defaultSystemBarsHost

val Fragment.defaultSystemBarsHost: SystemBarsHost? get() = activity?.defaultSystemBarsHost