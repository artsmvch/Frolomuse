package com.frolo.core.ui.application

import android.app.Activity
import android.app.Application
import androidx.annotation.GuardedBy
import com.frolo.ui.SimpleActivityLifecycleCallbacks
import java.util.concurrent.atomic.AtomicInteger

internal class ApplicationForegroundStatusRegistryImpl:
    ApplicationForegroundStatusRegistry,
    Application.ActivityLifecycleCallbacks by SimpleActivityLifecycleCallbacks() {

    private val lock = Any()

    @GuardedBy("lock")
    private var startedActivityCounter = 0
    @GuardedBy("lock")
    private val observers = HashSet<ApplicationForegroundStatusRegistry.Observer>()

    override val isInForeground: Boolean get() {
        return synchronized(lock) {
            startedActivityCounter > 0
        }
    }

    override val isInBackground: Boolean get() = !isInForeground

    override fun addObserver(observer: ApplicationForegroundStatusRegistry.Observer) {
        synchronized(lock) {
            observers.add(observer)
        }
    }

    override fun removeObserver(observer: ApplicationForegroundStatusRegistry.Observer) {
        synchronized(lock) {
            observers.remove(observer)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        synchronized(lock) {
            val newCount = ++startedActivityCounter
            if (newCount == 1) {
                dispatchApplicationForegroundStatus(isInForeground = true)
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {
        synchronized(lock) {
            val newCount = --startedActivityCounter
            if (newCount == 0) {
                dispatchApplicationForegroundStatus(isInForeground = false)
            }
        }
    }

    private fun dispatchApplicationForegroundStatus(isInForeground: Boolean) {
        synchronized(lock) {
            observers.forEach { observer ->
                observer.onApplicationForegroundStatusChanged(isInForeground = isInForeground)
            }
        }
    }
}