package com.frolo.logger.api


class CompositeLogDelegate(
    private val delegates: Collection<LogDelegate>
): LogDelegate {
    constructor(vararg delegates: LogDelegate): this(delegates.toList())

    override fun log(tag: String, level: LogLevel, msg: String, e: Throwable) {
        delegates.forEach { logDelegate ->
            logDelegate.log(tag, level, msg, e)
        }
    }

    override fun log(tag: String, level: LogLevel, msg: String) {
        delegates.forEach { logDelegate ->
            logDelegate.log(tag, level, msg)
        }
    }
}