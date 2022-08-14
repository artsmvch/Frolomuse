package com.frolo.audiofx2.impl

import com.frolo.audiofx2.AudioEffect2

internal abstract class BaseAudioEffect2Impl<E: android.media.audiofx.AudioEffect>: AudioEffect2 {
    private var lastAudioSessionId: Int? = null

    final fun applyToAudioSession(audioSessionId: Int) {
        if (lastAudioSessionId != audioSessionId) {
            onApplyToAudioSession(priority = 0, audioSessionId)
            lastAudioSessionId = audioSessionId
        }
    }

    protected abstract fun onApplyToAudioSession(priority: Int, audioSessionId: Int)
}