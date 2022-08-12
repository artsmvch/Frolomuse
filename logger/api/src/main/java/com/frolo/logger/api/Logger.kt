package com.frolo.logger.api

/**
 * Logs messages and errors for all [LogLevel] levels. Logging is delegated to a [LogDelegate]
 * defined in [Logger.params] so be sure to initialize the logger using the [Logger.init] method.
 */
object Logger {
    private const val DEFAULT_TAG = "<NULL>"
    @Volatile
    private var params: LoggerParams? = null

    @JvmStatic
    fun init(params: LoggerParams) {
        this.params = params
    }

    private fun buildTag(prefix: String?, tag: String?): String {
        val tag1 = prefix.orEmpty() + tag.orEmpty()
        return tag1.ifBlank { DEFAULT_TAG }
    }

    private fun logImpl(tag: String?, level: LogLevel, msg: String?, e: Throwable?) {
        params?.also { params ->
            params.logDelegate.log(
                tag = buildTag(prefix = params.tagPrefix, tag = tag),
                level = level,
                msg = msg,
                e = e
            )
        }
    }

    private fun logImpl(tag: String?, level: LogLevel, msg: String?) {
        params?.also { params ->
            params.logDelegate.log(
                tag = buildTag(prefix = params.tagPrefix, tag = tag),
                level = level,
                msg = msg
            )
        }
    }

    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "Tag mus be specified",
        replaceWith = ReplaceWith(
            expression = "Logger.e(tag, e)"
        )
    )
    @JvmStatic
    fun e(e: Throwable) {
        logImpl(
            tag = null,
            level = LogLevel.ERROR,
            msg = null,
            e = e
        )
    }

    @JvmStatic
    fun e(tag: String, e: Throwable) {
        logImpl(
            tag = tag,
            level = LogLevel.ERROR,
            msg = null,
            e = e
        )
    }

    @JvmStatic
    fun e(tag: String, msg: String?) {
        logImpl(
            tag = tag,
            level = LogLevel.ERROR,
            msg = msg
        )
    }

    @JvmStatic
    fun e(tag: String, msg: String?, e: Throwable?) {
        logImpl(
            tag = tag,
            level = LogLevel.ERROR,
            msg = msg,
            e = e
        )
    }

    @JvmStatic
    fun d(tag: String, msg: String?) {
        logImpl(
            tag = tag,
            level = LogLevel.DEBUG,
            msg = msg
        )
    }

    @JvmStatic
    fun i(tag: String, msg: String?) {
        logImpl(
            tag = tag,
            level = LogLevel.INFO,
            msg = msg
        )
    }

    @JvmStatic
    fun w(tag: String, msg: String?) {
        logImpl(
            tag = tag,
            level = LogLevel.WARN,
            msg = msg
        )
    }

    @JvmStatic
    fun v(tag: String, msg: String?) {
        logImpl(
            tag = tag,
            level = LogLevel.VERBOSE,
            msg = msg
        )
    }
}