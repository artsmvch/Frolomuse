package com.frolo.audiofx

object AudioFxFeature {
    private var audioFxProvider: AudioFxProvider? = null

    fun init(
        audioFxProvider: AudioFxProvider
    ) {
        this.audioFxProvider = audioFxProvider
    }

    internal fun getAudioFx(): AudioFx {
        val provider = this.audioFxProvider
            ?: throw NullPointerException("AudioFxProvider not found. " +
                    "Make sure you have AudioFxFeature initialized")
        return provider.provideAudioFx()
    }
}