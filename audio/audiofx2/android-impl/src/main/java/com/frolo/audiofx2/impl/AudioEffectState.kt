package com.frolo.audiofx2.impl

import android.content.SharedPreferences
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class AudioEffectState(
    private val preferences: SharedPreferences,
    private val key: String
) {

    private val lock = Any()
    private val isEnabledRef = AtomicBoolean(false)

    var isEnabled: Boolean = false
        set(value) {
            setEnabledImpl(value)
        }

    private fun setEnabledImpl(enabled: Boolean) {

    }
}