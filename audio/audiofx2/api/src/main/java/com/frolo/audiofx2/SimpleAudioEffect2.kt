package com.frolo.audiofx2

interface SimpleAudioEffect2: AudioEffect2 {
    val valueRange: EffectValueRange
    var value: Int
}