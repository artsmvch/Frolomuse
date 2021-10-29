package com.frolo.muse.di

import com.frolo.muse.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean


/**
 * The global access point to the [AppComponent] instance. The instance is initialized once
 * and lives as long as the application lives.
 */
lateinit var appComponent: AppComponent
    private set

private val isInitialized = AtomicBoolean(false)

private val isDebug: Boolean get() = BuildConfig.DEBUG

/**
 * Initializes the global access point to [AppComponent] instance. Must be called once when the application starts.
 */
fun initAppComponent(instance: AppComponent) {
    val wasInitializedEarlier = isInitialized.getAndSet(true)
    if (isDebug && wasInitializedEarlier) {
        throw IllegalStateException("initAppComponent is called more than once")
    }
    appComponent = instance
}