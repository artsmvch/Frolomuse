package com.frolo.muse

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.AnyThread
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logAppLaunched
import com.frolo.muse.repository.Preferences
import java.util.*


class FrolomuseActivityWatcher(
    private val preferences: Preferences,
    private val eventLogger: EventLogger
): Application.ActivityLifecycleCallbacks {

    private val _createdActivities = Collections.synchronizedList(ArrayList<Activity>())
    @get:AnyThread
    val createdActivities: List<Activity> get() = _createdActivities

    private val _startedActivities = Collections.synchronizedList(ArrayList<Activity>())
    @get:AnyThread
    val startedActivities: List<Activity> get() = _startedActivities

    private val _resumedActivities = Collections.synchronizedList(ArrayList<Activity>())
    @get:AnyThread
    val resumedActivities: List<Activity> get() = _resumedActivities

    private var activityResumeCount: Int = 0

    @get:AnyThread
    val foregroundActivity: Activity? get() {
        return resumedActivities.lastOrNull() ?: startedActivities.lastOrNull()
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        Logger.d(LOG_TAG, "On activity created: $activity")
        _createdActivities.add(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.d(LOG_TAG, "On activity started: $activity")
        _startedActivities.add(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.d(LOG_TAG, "On activity resumed: $activity")
        _resumedActivities.add(activity)
        if (activityResumeCount == 0) {
            // This is the first resume, we consider it as the actual launch of the app
            noteAppLaunch()
        }
        activityResumeCount++
    }

    override fun onActivityPaused(activity: Activity) {
        Logger.d(LOG_TAG, "On activity paused: $activity")
        _resumedActivities.remove(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        Logger.d(LOG_TAG, "On activity stopped: $activity")
        _startedActivities.remove(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
        Logger.d(LOG_TAG, "On activity save instance state: $activity")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Logger.d(LOG_TAG, "On activity destroyed: $activity")
        _createdActivities.remove(activity)
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