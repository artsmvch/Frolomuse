package com.frolo.core.ui.systembars

import android.app.Activity

@Deprecated("Use systemBarsHost extensions")
object SystemBarsHostFactory {
    fun of(activity: Activity): SystemBarsHost {
        // return SystemBarsHostImpl { activity.window }
        return activity.defaultSystemBarsHost!!
    }
}