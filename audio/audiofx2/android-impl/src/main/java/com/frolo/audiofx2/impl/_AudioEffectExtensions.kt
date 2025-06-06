package com.frolo.audiofx2.impl

import android.media.audiofx.AudioEffect

@Throws(Throwable::class)
internal fun checkForError(code: Int) {
    when (code) {
        AudioEffect.SUCCESS -> {
            // Success!
        }
        AudioEffect.ERROR -> {
            throw Exception("Something went wrong.")
        }
        AudioEffect.ERROR_NO_INIT -> {
            throw Exception("Operation failed due to bad object initialization.")
        }
        AudioEffect.ERROR_BAD_VALUE -> {
            throw Exception("Operation failed due to bad parameter value.")
        }
        AudioEffect.ERROR_INVALID_OPERATION -> {
            throw Exception("Operation failed because it was requested in wrong state.")
        }
        AudioEffect.ERROR_NO_MEMORY -> {
            throw Exception("Operation failed due to lack of memory.")
        }
        AudioEffect.ERROR_DEAD_OBJECT -> {
            throw Exception("Operation failed due to dead remote object.")
        }
    }
}

@Throws(Throwable::class)
internal fun AudioEffect.setEnabledOrThrow(enabled: Boolean) {
    val resultCode = setEnabled(enabled)
    checkForError(resultCode)
}