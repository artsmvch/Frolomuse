package com.frolo.audiofx2.impl

import com.frolo.audiofx2.AudioEffect2

fun interface AudioEffect2ErrorHandler {
    fun onAudioEffectError(effect: AudioEffect2, error: Throwable)
}