package com.frolo.logger.impl

import android.util.Log
import com.frolo.logger.api.LogDelegate
import com.frolo.logger.api.LogLevel


class ConsoleLogDelegate : LogDelegate {
    private fun logImpl(tag: String, level: LogLevel, msg: String?, e: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> {
                Log.v(tag, msg, e)
            }
            LogLevel.DEBUG -> {
                Log.d(tag, msg, e)
            }
            LogLevel.INFO -> {
                Log.i(tag, msg, e)
            }
            LogLevel.WARN -> {
                Log.w(tag, msg, e)
            }
            LogLevel.ERROR -> {
                Log.e(tag, msg, e)
            }
        }
    }

    override fun log(tag: String, level: LogLevel, msg: String?, e: Throwable?) {
        logImpl(
            tag = tag,
            level = level,
            msg = msg,
            e = e
        )
    }

    override fun log(tag: String, level: LogLevel, msg: String?) {
        logImpl(
            tag = tag,
            level = level,
            msg = msg,
            e = null
        )
    }
}