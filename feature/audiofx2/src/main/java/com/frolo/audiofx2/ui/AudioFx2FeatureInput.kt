package com.frolo.audiofx2.ui

import androidx.lifecycle.LiveData
import com.frolo.audiofx2.AudioFx2

interface AudioFx2FeatureInput {
    val audioFx2AttachInfo: LiveData<AudioFx2AttachInfo>
    val audioFx2: AudioFx2
}