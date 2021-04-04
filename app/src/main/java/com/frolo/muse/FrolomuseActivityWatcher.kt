package com.frolo.muse

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logAppLaunched
import com.frolo.muse.repository.Preferences


class FrolomuseActivityWatcher(
    private val preferences: Preferences,
    private val eventLogger: EventLogger
): Application.ActivityLifecycleCallbacks {

    private var activityResumeCount: Int = 0

    var foregroundActivity: Activity? = null
        private set

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Logger.d(LOG_TAG, "On activity created: $activity")
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.d(LOG_TAG, "On activity started: $activity")
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.d(LOG_TAG, "On activity resumed: $activity")
        if (activityResumeCount == 0) {
            // This is the first resume, we consider it as the actual launch of the app
            noteAppLaunch()
        }
        activityResumeCount++
        foregroundActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Logger.d(LOG_TAG, "On activity paused: $activity")
        if (foregroundActivity === activity) {
            foregroundActivity = null
        }
    }

    override fun onActivityStopped(activity: Activity) {
        Logger.d(LOG_TAG, "On activity stopped: $activity")
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        Logger.d(LOG_TAG, "On activity save instance state: $activity")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Logger.d(LOG_TAG, "On activity destroyed: $activity")
    }

    private fun noteAppLaunch() {
        val totalLaunchCount = preferences.openCount + 1 // +1 for the current launch
        preferences.openCount = totalLaunchCount
        eventLogger.logAppLaunched(totalLaunchCount)
        Logger.d(LOG_TAG, "Noted app launch: $totalLaunchCount")
    }

    companion object {
        private const val LOG_TAG = "FrolomuseActivityWatcher"
    }

}