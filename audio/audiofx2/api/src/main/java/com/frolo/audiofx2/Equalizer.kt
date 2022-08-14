package com.frolo.audiofx2

interface Equalizer: AudioEffect2 {
    val numberOfBands: Int
    val bandLevelRange: EffectValueRange
    fun getBandLevel(bandIndex: Int): Int
    fun setBandLevel(bandIndex: Int, level: Int)
    fun getFreqRange(bandIndex: Int): EffectValueRange
    fun addOnBandLevelChangeListener(listener: OnBandLevelChangeListener)
    fun removeOnBandLevelChangeListener(listener: OnBandLevelChangeListener)

    fun interface OnBandLevelChangeListener {
        fun onBandLevelChange(equalizer: Equalizer, band: Int, level: Int)
    }
}