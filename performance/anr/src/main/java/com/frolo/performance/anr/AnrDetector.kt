package com.frolo.performance.anr


/**
 * ANR-detector.
 */
interface AnrDetector {
    fun isRunning(): Boolean
    fun start()
    fun stop()
}