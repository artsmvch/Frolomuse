package com.frolo.audiofx

import androidx.lifecycle.LiveData
import com.frolo.audiofx2.AudioFx2

object AudioFx2Feature {
    private var audioFx2Provider: AudioFx2Provider? = null
    private var audioSessionDescription: LiveData<AudioSessionDescription>? = null

    fun init(
        audioFx2Provider: AudioFx2Provider,
        audioSessionDescription: LiveData<AudioSessionDescription>
    ) {
        this.audioFx2Provider = audioFx2Provider
        this.audioSessionDescription = audioSessionDescription
    }

    internal fun getAudioFx2(): AudioFx2 {
        val provider = this.audioFx2Provider
            ?: throw NullPointerException(
                propertyNotFoundLabel("AudioFx2Provider"))
        return provider.provideAudioFx2()
    }

    internal fun getAudioSessionDescription(): LiveData<AudioSessionDescription> {
        return this.audioSessionDescription
            ?: throw NullPointerException(
                propertyNotFoundLabel("AudioSessionDescription"))
    }

    private fun propertyNotFoundLabel(propertyName: String): String {
        return "$propertyName not found. " +
                "Make sure you have AudioFx2Feature initialized"
    }
}