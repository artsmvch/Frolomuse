package com.frolo.equalizerview


interface IEqualizer {

    val numberOfBands: Int
    val minBandLevelRange: Int
    val maxBandLevelRange: Int

    fun getBandLevel(band: Short): Short
    fun setBandLevel(band: Short, level: Short)
    fun getBandFreqRange(band: Short): IntArray

    fun registerObserver(observer: Observer)
    fun unregisterObserver(observer: Observer)

    interface Observer {
        fun onBandLevelChanged(band: Short, level: Short)
    }
}