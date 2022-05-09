package com.frolo.muse.memory


object MemoryObserverRegistryStub : MemoryObserverRegistry {
    override fun addWeakObserver(observer: MemoryObserver) = Unit
    override fun removeWeakObserver(observer: MemoryObserver) = Unit
}