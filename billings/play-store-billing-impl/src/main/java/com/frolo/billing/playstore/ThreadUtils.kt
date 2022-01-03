package com.frolo.billing.playstore

import android.os.Handler
import android.os.Looper


internal object ThreadUtils {

    private const val isDebug: Boolean = BuildConfig.DEBUG

    private val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    fun isMainThread(): Boolean {
        return Thread.currentThread() == mainHandler.looper.thread
    }

    fun assertMainThread() {
        if (isDebug && !isMainThread()) {
            throw IllegalStateException("Called on a background thread")
        }
    }

    fun assertBackgroundThread() {
        if (isDebug && isMainThread()) {
            throw IllegalStateException("Called on the main thread")
        }
    }
}