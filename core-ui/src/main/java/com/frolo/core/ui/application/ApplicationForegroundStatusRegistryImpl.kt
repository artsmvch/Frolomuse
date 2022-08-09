package com.frolo.core.ui.application

import android.app.Activity
import android.app.Application
import androidx.annotation.GuardedBy
import com.frolo.ui.SimpleActivityLifecycleCallbacks
import java.util.concurrent.atomic.AtomicInteger

internal class ApplicationForegroundStatusRegistryImpl:
    ApplicationForegroundStatusRegistry,
    Application.ActivityLifecycleCallbacks by SimpleActivityLifecycleCallbacks() {

    private val startedActivityCounter = AtomicInteger(0)

    private val observersLock = Any()
    @GuardedBy("observersLock")
    private val observers = HashSet<ApplicationForegroundStatusRegistry.Observer>()

    override val isInForeground: Boolean get() = startedActivityCounter.get() > 0

    override val isInBackground: Boolean get() = !isInForeground

    override fun addObserver(observer: ApplicationForegroundStatusRegistry.Observer) {
        synchronized(observersLock) {
            observers.add(observer)
        }
    }

    override fun removeObserver(observer: ApplicationForegroundStatusRegistry.Observer) {
        synchronized(observersLock) {
            observers.remove(observer)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        val newCount = startedActivityCounter.incrementAndGet()
        if (newCount == 1) {
            dispatchApplicationForegroundStatus(isInForeground = true)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        val newCount = startedActivityCounter.decrementAndGet()
        if (newCount == 0) {
            dispatchApplicationForegroundStatus(isInForeground = false)
        }
    }

    private fun dispatchApplicationForegroundStatus(isInForeground: Boolean) {
        synchronized(observersLock) {
            observers.forEach { observer ->
                observer.onApplicationForegroundStatusChanged(isInForeground = isInForeground)
            }
        }
    }
}