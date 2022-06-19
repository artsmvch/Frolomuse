package com.frolo.performance.anr

internal object StackTraceHelper {

    fun collectStackTrace(thread: Thread): Array<out StackTraceElement>? {
        return thread.stackTrace
    }

    fun collectStackTrace(): Array<out StackTraceElement>? {
        val throwable = Throwable()
        return throwable.stackTrace
    }
}