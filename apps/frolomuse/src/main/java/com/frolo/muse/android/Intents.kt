package com.frolo.muse.android

import android.content.Intent


fun Intent.getByteExtraOrNull(key: String): Byte? {
    return if (hasExtra(key)) getByteExtra(key, 0) else null
}

fun Intent.getShortExtraOrNull(key: String): Short? {
    return if (hasExtra(key)) getShortExtra(key, 0) else null
}

fun Intent.getIntExtraOrNull(key: String): Int? {
    return if (hasExtra(key)) getIntExtra(key, 0) else null
}

fun Intent.getLongExtraOrNull(key: String): Long? {
    return if (hasExtra(key)) getLongExtra(key, 0L) else null
}

fun Intent.getFloatExtraOrNull(key: String): Float? {
    return if (hasExtra(key)) getFloatExtra(key, 0f) else null
}

fun Intent.getDoubleExtraOrNull(key: String): Double? {
    return if (hasExtra(key)) getDoubleExtra(key, 0.0) else null
}

fun Intent.getStringExtraOrNull(key: String): String? {
    return if (hasExtra(key)) getStringExtra(key) else null
}