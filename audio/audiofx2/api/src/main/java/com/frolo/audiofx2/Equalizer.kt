package com.frolo.audiofx2

interface Equalizer: AudioEffect2, EqualizerPresetStorage {
    val numberOfBands: Int
    val bandLevelRange: ValueRange
    fun getBandLevelsSnapshot(): Map<Int, Int>
    fun getBandLevel(bandIndex: Int): Int
    fun setBandLevel(bandIndex: Int, level: Int)
    fun getFreqRange(bandIndex: Int): ValueRange
    fun addOnBandLevelChangeListener(listener: OnBandLevelChangeListener)
    fun removeOnBandLevelChangeListener(listener: OnBandLevelChangeListener)
    fun addOnPresetUsedListener(listener: OnPresetUsedListener)
    fun removeOnPresetUsedListener(listener: OnPresetUsedListener)

    fun interface OnBandLevelChangeListener {
        fun onBandLevelChange(equalizer: Equalizer, band: Int, level: Int)
    }

    fun interface OnPresetUsedListener {
        fun onPresetUsed(equalizer: Equalizer, preset: EqualizerPreset)
    }
}