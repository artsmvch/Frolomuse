package com.frolo.billing.playstore

import android.util.Log


internal class Logger private constructor(
    private val tag: String,
    private val isEnabled: Boolean
) {

    fun d(msg: String) {
        if (isEnabled) Log.e(tag, msg)
    }

    fun e(msg: String) {
        if (isEnabled) Log.e(tag, msg)
    }

    fun e(tr: Throwable) {
        if (isEnabled) Log.e(tag, "", tr)
    }

    fun e(msg: String, tr: Throwable) {
        if (isEnabled) Log.e(tag, msg, tr)
    }

    companion object {
        fun create(tag: String, isEnabled: Boolean): Logger {
            return Logger(tag, isEnabled)
        }
    }

}