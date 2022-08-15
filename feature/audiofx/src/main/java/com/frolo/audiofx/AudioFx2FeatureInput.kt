package com.frolo.audiofx

import androidx.lifecycle.LiveData
import com.frolo.audiofx2.AudioFx2

interface AudioFx2FeatureInput {
    val audioSessionDescription: LiveData<AudioSessionDescription>
    val audioFx2: AudioFx2
}