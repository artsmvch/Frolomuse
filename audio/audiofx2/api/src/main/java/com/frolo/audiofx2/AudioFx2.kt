package com.frolo.audiofx2

interface AudioFx2 {
    val equalizer: Equalizer?
    val bassBoost: BassBoost?
    val virtualizer: Virtualizer?
    val loudness: Loudness?
    val reverb: Reverb?
}