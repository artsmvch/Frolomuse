package com.frolo.logger.api

/**
 * Message and error logging protocol.
 */
interface LogDelegate {
    fun log(tag: String, level: LogLevel, msg: String?, e: Throwable?)
    fun log(tag: String, level: LogLevel, msg: String?)

    object None : LogDelegate {
        override fun log(tag: String, level: LogLevel, msg: String?, e: Throwable?) = Unit
        override fun log(tag: String, level: LogLevel, msg: String?) = Unit
    }
}