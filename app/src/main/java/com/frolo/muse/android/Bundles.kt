package com.frolo.muse.android

import android.os.Bundle


fun Bundle.getByteOrNull(key: String): Byte? {
    return if (containsKey(key)) getByte(key) else null
}

fun Bundle.getShortOrNull(key: String): Short? {
    return if (containsKey(key)) getShort(key) else null
}

fun Bundle.getIntOrNull(key: String): Int? {
    return if (containsKey(key)) getInt(key) else null
}

fun Bundle.getLongOrNull(key: String): Long? {
    return if (containsKey(key)) getLong(key) else null
}

fun Bundle.getFloatOrNull(key: String): Float? {
    return if (containsKey(key)) getFloat(key) else null
}

fun Bundle.getDoubleOrNull(key: String): Double? {
    return if (containsKey(key)) getDouble(key) else null
}

fun Bundle.getStringOrNull(key: String): String? {
    return if (containsKey(key)) getString(key) else null
}