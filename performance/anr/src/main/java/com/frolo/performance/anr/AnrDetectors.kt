package com.frolo.performance.anr

import android.content.Context
import android.os.Looper

object AnrDetectors {
    const val PING_INTERVAL_MILLIS = 20L
    const val RESPONSE_THRESHOLD_MILLIS = 1000L

    private const val USE_EXPERIMENTAL_DETECTOR = true

    @JvmStatic
    fun create(looper: Looper, uiContextProvider: () -> Context?): AnrDetector {
        val config = AnrDetectorConfig(
            targetLooper = looper,
            pingIntervalMillis = PING_INTERVAL_MILLIS,
            responseThresholdMillis = RESPONSE_THRESHOLD_MILLIS,
            listener = DefaultAnrListener(uiContextProvider)
        )
        return createImpl(config)
    }

    @JvmStatic
    fun create(looper: Looper, listener: OnAnrDetectedListener): AnrDetector {
        val config = AnrDetectorConfig(
            targetLooper = looper,
            pingIntervalMillis = PING_INTERVAL_MILLIS,
            responseThresholdMillis = RESPONSE_THRESHOLD_MILLIS,
            listener = listener
        )
        return createImpl(config)
    }

    private fun createImpl(config: AnrDetectorConfig): AnrDetector {
        return if (USE_EXPERIMENTAL_DETECTOR) {
            AnrDetectorExperimentalImpl(config)
        } else {
            AnrDetectorImpl(config)
        }
    }
}