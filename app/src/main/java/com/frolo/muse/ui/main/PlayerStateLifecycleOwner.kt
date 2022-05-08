package com.frolo.muse.ui.main

import android.app.Activity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.frolo.ui.ActivityUtils
import com.frolo.ui.SimpleActivityLifecycleCallbacks


internal class PlayerStateLifecycleOwner(
    private val activity: Activity
) : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        val activityLifecycleObserver = object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityDestroyed(activity: Activity) {
                activity.application.unregisterActivityLifecycleCallbacks(this)

                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        }
        activity.application.registerActivityLifecycleCallbacks(activityLifecycleObserver)

        if (!ActivityUtils.isFinishingOrDestroyed(activity)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } else {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}