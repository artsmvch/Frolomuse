package com.frolo.audiofx

import androidx.lifecycle.LiveData
import com.frolo.audiofx2.AudioFx2

object AudioFx2Feature {
    private var input: AudioFx2FeatureInput? = null

    fun init(input: AudioFx2FeatureInput) {
        this.input = input
    }

    private fun requireInput(): AudioFx2FeatureInput {
        return this.input ?: throw NullPointerException(
            "AudioFx2FeatureInput not found. Make sure you have AudioFx2Feature initialized")
    }

    internal fun getAudioFx2(): AudioFx2 {
        return requireInput().audioFx2
    }

    internal fun getAttachInfoLiveData(): LiveData<AudioFx2AttachInfo> {
        return requireInput().audioFx2AttachInfo
    }
}