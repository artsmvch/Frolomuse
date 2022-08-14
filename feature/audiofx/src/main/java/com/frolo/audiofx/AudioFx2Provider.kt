package com.frolo.audiofx

import com.frolo.audiofx2.AudioFx2

fun interface AudioFx2Provider {
    fun provideAudioFx2(): AudioFx2
}