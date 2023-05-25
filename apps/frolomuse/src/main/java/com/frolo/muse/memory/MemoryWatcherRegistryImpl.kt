package com.frolo.muse.memory

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import androidx.annotation.GuardedBy
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class MemoryWatcherRegistryImpl(
    private val application: Application
) : MemoryWatcherRegistry {

    private val componentCallbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
        }

        override fun onLowMemory() {
            synchronized(weakWatchers) {
                weakWatchers.forEach { observer ->
                    observer.noteLowMemory()
                }
            }
        }

        override fun onTrimMemory(level: Int) {
        }

    }

    private val isActivated = AtomicBoolean(false)
    @get:GuardedBy("weakWatchers")
    private val weakWatchers by lazy {
        // Lazily activate when the watchers are touched
        Collections.newSetFromMap(WeakHashMap<MemoryWatcher, Boolean>()).apply { activate() }
    }

    fun activate() {
        if (!isActivated.getAndSet(true)) {
            application.registerComponentCallbacks(componentCallbacks)
        }
    }

    fun deactivate() {
        if (isActivated.getAndSet(false)) {
            application.unregisterComponentCallbacks(componentCallbacks)
        }
    }

    override fun addWeakWatcher(watcher: MemoryWatcher) {
        synchronized(weakWatchers) {
            weakWatchers.add(watcher)
        }
    }

    override fun removeWeakWatcher(watcher: MemoryWatcher) {
        synchronized(weakWatchers) {
            weakWatchers.remove(watcher)
        }
    }
}