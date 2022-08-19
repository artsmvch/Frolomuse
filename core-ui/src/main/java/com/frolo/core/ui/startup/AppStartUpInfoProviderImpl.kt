package com.frolo.core.ui.startup

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import com.frolo.ui.SimpleActivityLifecycleCallbacks
import java.util.concurrent.atomic.AtomicLong

internal class AppStartUpInfoProviderImpl(
    private val application: Application,
) : AppStartUpInfoProvider {

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val coldStartCountRef by lazy {
        AtomicLong().apply {
            set(prefs.getLong(KEY_COLD_START_COUNT, 0L))
        }
    }
    override val coldStartCount: Long get() = coldStartCountRef.get()

    private val warmStartCountRef by lazy {
        AtomicLong().apply {
            set(prefs.getLong(KEY_WARM_START_COUNT, 0L))
        }
    }
    override val warmStartCount: Long get() = warmStartCountRef.get()

    private val hotStartCountRef by lazy {
        AtomicLong().apply {
            set(prefs.getLong(KEY_HOT_START_COUNT, 0L))
        }
    }
    override val hotStartCount: Long get() = hotStartCountRef.get()

    private val activityLifecycleCallbacks = object : SimpleActivityLifecycleCallbacks() {
        // The very first start is cold, so we skip both warm and hot
        var skipWarmStart = true
        var skipHotStart = true

        var createdCount: Int = 0
        var startedCount: Int = 0

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (createdCount == 0 && !skipWarmStart) {
                dispatchWarmStart()
            }
            createdCount++
            skipWarmStart = false
            skipHotStart = true
        }

        override fun onActivityStarted(activity: Activity) {
            if (startedCount == 0 && !skipHotStart) {
                dispatchHotStart()
            }
            startedCount++
            skipHotStart = false
        }

        override fun onActivityStopped(activity: Activity) {
            startedCount--
        }

        override fun onActivityDestroyed(activity: Activity) {
            createdCount--
        }
    }

    fun start() {
        dispatchColdStart()
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private fun incrementCounter(counterRef: AtomicLong, counterKey: String): Long {
        val newCount = counterRef.incrementAndGet()
        prefs.edit { putLong(counterKey, newCount) }
        return newCount
    }

    private fun dispatchColdStart() {
        incrementCounter(coldStartCountRef, KEY_COLD_START_COUNT).also { newCount ->
            Log.d(LOG_TAG, "Cold start detected: total=$newCount")
        }
    }

    private fun dispatchWarmStart() {
        incrementCounter(warmStartCountRef, KEY_WARM_START_COUNT).also { newCount ->
            Log.d(LOG_TAG, "Warm start detected: total=$newCount")
        }
    }

    private fun dispatchHotStart() {
        incrementCounter(hotStartCountRef, KEY_HOT_START_COUNT).also { newCount ->
            Log.d(LOG_TAG, "Hot start detected: total=$newCount")
        }
    }

    companion object {
        private const val LOG_TAG = "AppStartUpInfoProviderImpl"

        private const val PREFS_NAME = "com.frolo.core.ui.startup"

        private const val KEY_COLD_START_COUNT = "cold_start_count"
        private const val KEY_WARM_START_COUNT = "warm_start_count"
        private const val KEY_HOT_START_COUNT = "hot_start_count"
    }
}