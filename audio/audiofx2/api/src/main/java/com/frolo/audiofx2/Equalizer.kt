package com.frolo.audiofx2

interface Equalizer: AudioEffect2, EqualizerPresetStorage {
    val numberOfBands: Int
    val bandLevelRange: ValueRange
    fun getBandLevel(bandIndex: Int): Int
    fun setBandLevel(bandIndex: Int, level: Int)
    fun getFreqRange(bandIndex: Int): ValueRange
    fun addOnBandLevelChangeListener(listener: OnBandLevelChangeListener)
    fun removeOnBandLevelChangeListener(listener: OnBandLevelChangeListener)

    fun interface OnBandLevelChangeListener {
        fun onBandLevelChange(equalizer: Equalizer, band: Int, level: Int)
    }
}