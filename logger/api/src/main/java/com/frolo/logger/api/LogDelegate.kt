package com.frolo.logger.api

interface LogDelegate {
    fun log(tag: String, level: LogLevel, msg: String?, e: Throwable?)
    fun log(tag: String, level: LogLevel, msg: String?)
}