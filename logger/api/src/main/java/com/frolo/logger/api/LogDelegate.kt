package com.frolo.logger.api

/**
 * Message and error logging protocol.
 */
interface LogDelegate {
    fun log(tag: String, level: LogLevel, msg: String?, e: Throwable?)
    fun log(tag: String, level: LogLevel, msg: String?)
}