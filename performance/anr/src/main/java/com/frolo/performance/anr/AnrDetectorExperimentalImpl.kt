package com.frolo.performance.anr

import android.util.Log
import android.util.Printer
import java.util.concurrent.atomic.AtomicBoolean


internal class AnrDetectorExperimentalImpl(
    private val config: AnrDetectorConfig
) : AnrDetector {

    private val isRunningRef = AtomicBoolean(false)
    private val printer = Printer { msg ->
        when {
            msg.startsWith(LOOP_MESSAGE_START_PREFIX) -> {
                loopStartTime = System.currentTimeMillis()
            }
            msg.startsWith(LOOP_MESSAGE_END_PREFIX) -> {
                val loopTime = System.currentTimeMillis() - loopStartTime
                checkLoopTime(loopTime)
            }
            else -> {
                Log.e(LOG_TAG, "Unexpected looper message: $msg")
            }
        }
    }
    private var loopStartTime: Long = 0

    override fun isRunning(): Boolean {
        return isRunningRef.get()
    }

    override fun start() {
        if (!isRunningRef.getAndSet(true)) {
            config.targetLooper.setMessageLogging(printer)
        }
    }

    override fun stop() {
        if (isRunningRef.getAndSet(false)) {
            config.targetLooper.setMessageLogging(null)
        }
    }

    private fun checkLoopTime(loopTime: Long) {
        if (loopTime >= config.responseThresholdMillis) {
            val info = AnrInfo(
                responseTime = loopTime,
                stackTrace = null //StackTraceHelper.collectStackTrace()
            )
            config.listener.onAnrDetected(
                looper = config.targetLooper,
                info = info
            )
        }
    }

    private companion object {
        private const val LOG_TAG = "AnrDetectorExperimentalImpl"

        private const val LOOP_MESSAGE_START_PREFIX = ">>>>> Dispatching to"
        private const val LOOP_MESSAGE_END_PREFIX = "<<<<< Finished to"
    }
}