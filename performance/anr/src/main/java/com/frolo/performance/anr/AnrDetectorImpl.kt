package com.frolo.performance.anr

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean


internal class AnrDetectorImpl(
    private val config: AnrDetectorConfig
) : AnrDetector {

    @Volatile
    private var thread: ThreadImpl? = null

    @Synchronized
    override fun isRunning(): Boolean {
        // return isRunningRef.get()
        return thread?.isOrWillBeRunning ?: false
    }

    @Synchronized
    override fun start() {
        val currThread = this.thread
        if (currThread != null && currThread.isOrWillBeRunning) {
            return
        }
        val newThread = ThreadImpl(debug = false, config)
        this.thread = newThread
        newThread.start()
    }

    @Synchronized
    override fun stop() {
        thread?.stopImpl()
        thread = null
    }

    private class ThreadImpl(
        val debug: Boolean,
        val config: AnrDetectorConfig
    ) : Thread() {

        private val isOrWillBeRunningRef = AtomicBoolean(false)

        private val pingHandler = Handler(config.targetLooper)
        private val callback = Runnable {
            if (debug) Log.d(LOG_TAG, "Callback fired")
            callbackFired.set(true)
            synchronized(waiter) {
                waiter.notify()
            }
        }
        private val callbackFired = AtomicBoolean(false)
        private val waiter = java.lang.Object()

        val isOrWillBeRunning: Boolean get() = isOrWillBeRunningRef.get()

        override fun start() {
            isOrWillBeRunningRef.set(true)
            super.start()
        }

        fun stopImpl() {
            isOrWillBeRunningRef.set(false)
        }

        override fun run() {
            try {
                while (isOrWillBeRunningRef.get()) {
                    loopOnce()
                }
            } catch (e: Throwable) {
                Log.e(LOG_TAG, "", e)
            }
        }

        private fun loopOnce() {
            val startTime = System.currentTimeMillis()
            synchronized(waiter) {
                if (config.pingIntervalMillis > 0) {
                    val updateMillis = SystemClock.uptimeMillis() + config.pingIntervalMillis
                    pingHandler.postAtTime(callback, updateMillis)
                } else {
                    pingHandler.post(callback)
                }

                if (debug) Log.d(LOG_TAG, "Waiting...")
                // waiter.wait(config.responseTimeMillis + ERROR_TIME)
                waiter.wait()
            }
            val fired = callbackFired.getAndSet(false)
            val endTime = System.currentTimeMillis()
            val responseTime = endTime - startTime
            if (debug) Log.d(LOG_TAG, "Calculated response time: ")
            checkResponseTime(fired, responseTime)
        }

        private fun checkResponseTime(fired: Boolean, responseTime: Long) {
            if (!isOrWillBeRunningRef.get()) {
                return
            }
            if (!fired || responseTime > config.responseThresholdMillis) {
                val info = AnrInfo(
                    responseTime = responseTime,
                    stackTrace = StackTraceHelper.collectStackTrace(
                        thread = config.targetLooper.thread
                    )
                )
                config.listener.onAnrDetected(config.targetLooper, info)
            }
        }
    }

    private companion object {
        private const val LOG_TAG = "AndDetectorImpl"

        const val ERROR_TIME = 50L
    }
}