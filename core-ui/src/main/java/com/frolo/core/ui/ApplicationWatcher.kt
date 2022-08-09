package com.frolo.core.ui

import android.app.Activity
import android.content.Context
import com.frolo.core.ui.activity.ActivityWatcher

object ApplicationWatcher {
    @JvmStatic
    val applicationContext: Context get() {
        return ApplicationWatcherImpl.instance.requireApplicationContext()
    }

    @JvmStatic
    val activityWatcher: ActivityWatcher get() {
        return ApplicationWatcherImpl.instance.activityWatcher
    }

    @JvmStatic
    val foregroundActivity: Activity? get() = activityWatcher.foregroundActivity

    @JvmStatic
    val isInForeground: Boolean get() {
        // The app is considered foreground if it has at least 1 activity started
        return activityWatcher.startedActivities.count() > 0
    }
}