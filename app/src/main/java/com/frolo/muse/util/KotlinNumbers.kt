package com.frolo.muse.util


internal fun Float.ifNaN(otherValue: Float): Float {
    return if (isNaN()) otherValue else this
}

internal fun Double.ifNaN(otherValue: Double): Double {
    return if (isNaN()) otherValue else this
}