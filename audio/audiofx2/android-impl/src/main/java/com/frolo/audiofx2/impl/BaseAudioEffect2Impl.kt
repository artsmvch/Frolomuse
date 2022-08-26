package com.frolo.audiofx2.impl

import com.frolo.audiofx2.AudioEffect2

internal abstract class BaseAudioEffect2Impl<E: android.media.audiofx.AudioEffect>:
    AudioEffect2, AudioFx2AttachProtocol {
    @Volatile
    private var lastAudioSessionId: Int? = null

    final override fun attachTo(target: AudioFx2AttachTarget) {
        if (lastAudioSessionId != target.sessionId) {
            onAttachTo(target)
            lastAudioSessionId = target.sessionId
        }
    }

    protected abstract fun onAttachTo(target: AudioFx2AttachTarget)

    final override fun release() {
        onRelease()
        lastAudioSessionId = null
    }

    protected abstract fun onRelease()
}