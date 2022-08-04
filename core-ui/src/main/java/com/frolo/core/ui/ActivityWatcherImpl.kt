package com.frolo.core.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.AnyThread
import java.util.*


class ActivityWatcherImpl: Application.ActivityLifecycleCallbacks,
    ActivityWatcher {

    private val _createdActivities = Collections.synchronizedList(ArrayList<Activity>())
    private val _startedActivities = Collections.synchronizedList(ArrayList<Activity>())
    private val _resumedActivities = Collections.synchronizedList(ArrayList<Activity>())

    @AnyThread
    override fun getCreatedActivities(): List<Activity> {
        return _createdActivities
    }

    @AnyThread
    override fun getStartedActivities(): List<Activity> {
        return _startedActivities
    }

    @AnyThread
    override fun getResumedActivities(): List<Activity> {
        return _resumedActivities
    }

    @AnyThread
    override fun getForegroundActivity(): Activity? {
        return resumedActivities.lastOrNull() ?: startedActivities.lastOrNull()
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        logMessage("On activity created: $activity")
        _createdActivities.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        logMessage("On activity started: $activity")
        _startedActivities.add(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        logMessage("On activity resumed: $activity")
        _resumedActivities.add(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        logMessage("On activity paused: $activity")
        _resumedActivities.remove(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        logMessage("On activity stopped: $activity")
        _startedActivities.remove(activity)
        if (activity.isFinishing) {
            // There is no guarantee that the onDestroy will be called
            _createdActivities.remove(activity)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        logMessage("On activity save instance state: $activity")
    }

    override fun onActivityDestroyed(activity: Activity) {
        logMessage("On activity destroyed: $activity")
        _createdActivities.remove(activity)
    }
    
    private fun logMessage(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, msg)
        }
    }

    companion object {
        private const val LOG_TAG = "ActivityWatcherImpl"
    }

}