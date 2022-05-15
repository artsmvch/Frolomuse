package com.frolo.muse.memory

interface MemoryWatcherRegistry {
    fun addWeakWatcher(watcher: MemoryWatcher)
    fun removeWeakWatcher(watcher: MemoryWatcher)
}