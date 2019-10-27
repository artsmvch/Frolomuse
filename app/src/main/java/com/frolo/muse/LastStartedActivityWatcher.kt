package com.frolo.muse

import android.app.Activity
import android.app.Application
import android.os.Bundle


class LastStartedActivityWatcher : Application.ActivityLifecycleCallbacks {

    var currentLast: Activity? = null
        private set

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStarted(activity: Activity) {
        currentLast = activity
    }

    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) = Unit

    override fun onActivityStopped(activity: Activity) {
        if (currentLast === activity) {
            currentLast = null
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

}