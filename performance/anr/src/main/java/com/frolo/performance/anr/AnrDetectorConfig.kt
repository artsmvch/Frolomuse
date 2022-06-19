package com.frolo.performance.anr

import android.os.Looper


internal data class AnrDetectorConfig(
    // Target loop to test response time
    val targetLooper: Looper,
    // Small ping interval is good for accuracy, but loads the message queue
    val pingIntervalMillis: Long,
    // The min response time for the target looper to be considered unresponsive
    val responseThresholdMillis: Long,
    val listener: OnAnrDetectedListener
)