package com.frolo.core.ui

import android.app.Activity
import android.content.Context
import com.frolo.core.ui.activity.ActivityWatcher
import com.frolo.core.ui.application.ApplicationForegroundStatusRegistry
import com.frolo.core.ui.startup.AppStartUpInfoProvider

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
    val applicationForegroundStatusRegistry: ApplicationForegroundStatusRegistry get() {
        return ApplicationWatcherImpl.instance.applicationForegroundStatusRegistry
    }

    @JvmStatic
    val foregroundActivity: Activity? get() = activityWatcher.foregroundActivity

    @JvmStatic
    val isInForeground: Boolean get() {
        return applicationForegroundStatusRegistry.isInForeground
    }

    @JvmStatic
    val isInBackground: Boolean get() = !isInForeground

    @JvmStatic
    val appStartUpInfoProvider: AppStartUpInfoProvider get() {
        return ApplicationWatcherImpl.instance.appStartUpInfoProvider
    }
}