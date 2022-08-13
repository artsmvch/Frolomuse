package com.frolo.audiofx2

interface Equalizer: AudioEffect2 {
    val numberOfBands: Int
    fun getBandLevel(bandIndex: Int): Int
    fun setBandLevel(bandIndex: Int, level: Int)
    fun getFreqRange(bandIndex: Int): EffectValueRange
}