package com.frolo.audiofx

import androidx.lifecycle.LiveData
import com.frolo.audiofx2.AudioFx2

interface AudioFx2FeatureInput {
    val audioSessionInfo: LiveData<AudioSessionInfo>
    val audioFx2: AudioFx2
}