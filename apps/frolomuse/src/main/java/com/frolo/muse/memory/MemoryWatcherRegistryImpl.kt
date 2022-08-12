package com.frolo.muse.memory

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import java.util.*


class MemoryWatcherRegistryImpl(
    private val application: Application
) : MemoryWatcherRegistry {

    private val componentCallbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
        }

        override fun onLowMemory() {
            weakWatchers.forEach { observer ->
                observer.noteLowMemory()
            }
        }

        override fun onTrimMemory(level: Int) {
        }

    }

    private val weakWatchers = Collections.newSetFromMap(WeakHashMap<MemoryWatcher, Boolean>())

    fun activate() {
        application.registerComponentCallbacks(componentCallbacks)
    }

    fun deactivate() {
        application.unregisterComponentCallbacks(componentCallbacks)
    }

    override fun addWeakWatcher(watcher: MemoryWatcher) {
        weakWatchers.add(watcher)
    }

    override fun removeWeakWatcher(watcher: MemoryWatcher) {
        weakWatchers.remove(watcher)
    }
}