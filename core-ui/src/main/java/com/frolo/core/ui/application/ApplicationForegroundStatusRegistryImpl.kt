package com.frolo.core.ui.application

import android.app.Activity
import android.app.Application
import com.frolo.ui.SimpleActivityLifecycleCallbacks

internal class ApplicationForegroundStatusRegistryImpl:
    ApplicationForegroundStatusRegistry,
    Application.ActivityLifecycleCallbacks by SimpleActivityLifecycleCallbacks() {

    private var startedActivityCount: Int = 0
    private val observers = HashSet<ApplicationForegroundStatusRegistry.Observer>()

    override val isInForeground: Boolean get() = startedActivityCount > 0

    override val isInBackground: Boolean get() = !isInForeground

    override fun addObserver(observer: ApplicationForegroundStatusRegistry.Observer) {
        observers.add(observer)
    }

    override fun removeObserver(observer: ApplicationForegroundStatusRegistry.Observer) {
        observers.remove(observer)
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivityCount++
        if (startedActivityCount == 1) {
            dispatchApplicationForegroundStatus(isInForeground = true)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount--
        if (startedActivityCount == 0) {
            dispatchApplicationForegroundStatus(isInForeground = false)
        }
    }

    private fun dispatchApplicationForegroundStatus(isInForeground: Boolean) {
        observers.forEach { observer ->
            observer.onApplicationForegroundStatusChanged(isInForeground = isInForeground)
        }
    }
}