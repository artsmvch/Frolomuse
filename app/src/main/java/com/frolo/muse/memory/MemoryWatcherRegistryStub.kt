package com.frolo.muse.memory


object MemoryWatcherRegistryStub : MemoryWatcherRegistry {
    override fun addWeakWatcher(watcher: MemoryWatcher) = Unit
    override fun removeWeakWatcher(watcher: MemoryWatcher) = Unit
}