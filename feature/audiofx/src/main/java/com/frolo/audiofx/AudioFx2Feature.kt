package com.frolo.audiofx

import com.frolo.audiofx2.AudioFx2

object AudioFx2Feature {
    private var audioFx2Provider: AudioFx2Provider? = null

    fun init(
        audioFx2Provider: AudioFx2Provider
    ) {
        this.audioFx2Provider = audioFx2Provider
    }

    internal fun getAudioFx2(): AudioFx2 {
        val provider = this.audioFx2Provider
            ?: throw NullPointerException("AudioFx2Provider not found. " +
                    "Make sure you have AudioFx2Feature initialized")
        return provider.provideAudioFx2()
    }
}