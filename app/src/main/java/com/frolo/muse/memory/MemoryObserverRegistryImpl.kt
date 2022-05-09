package com.frolo.muse.memory

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import java.util.*


class MemoryObserverRegistryImpl(
    private val application: Application
) : MemoryObserverRegistry {

    private val componentCallbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
        }

        override fun onLowMemory() {
            weakObservers.forEach { observer ->
                observer.noteLowMemory()
            }
        }

        override fun onTrimMemory(level: Int) {
        }

    }

    private val weakObservers = Collections.newSetFromMap(WeakHashMap<MemoryObserver, Boolean>())

    fun activate() {
        application.registerComponentCallbacks(componentCallbacks)
    }

    fun deactivate() {
        application.unregisterComponentCallbacks(componentCallbacks)
    }

    override fun addWeakObserver(observer: MemoryObserver) {
        weakObservers.add(observer)
    }

    override fun removeWeakObserver(observer: MemoryObserver) {
        weakObservers.remove(observer)
    }
}