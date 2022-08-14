package com.frolo.audiofx2.impl

interface AudioSessionApplier {
    fun applyToAudioSession(audioSessionId: Int)
    fun release()
}