package com.frolo.performance.anr


class AnrInfo(
    val responseTime: Long,
    val cause: String? = null,
    // Experimental
    val stackTrace: Array<out StackTraceElement>? = null
) {

    fun toStringDetailed(): String {
        return "AnrInfo(" +
                "responseTime=$responseTime, " +
                "cause=$cause, " +
                "stackTrace=${stackTrace?.let(::stackTraceToString)})"
    }

    private fun stackTraceToString(stackTrace: Array<out StackTraceElement>): String {
        val builder = StringBuilder()
        for (element in stackTrace) {
            builder.append(element.toString())
            builder.append("\n")
        }
        return builder.toString()
    }
}