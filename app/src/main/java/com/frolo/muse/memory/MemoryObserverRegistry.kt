package com.frolo.muse.memory

interface MemoryObserverRegistry {
    fun addWeakObserver(observer: MemoryObserver)
    fun removeWeakObserver(observer: MemoryObserver)
}