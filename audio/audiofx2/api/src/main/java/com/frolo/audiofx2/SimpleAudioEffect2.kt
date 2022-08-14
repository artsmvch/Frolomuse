package com.frolo.audiofx2

interface SimpleAudioEffect2: AudioEffect2 {
    val valueRange: ValueRange
    var value: Int
    fun addOnEffectValueChangeListener(listener: OnEffectValueChangeListener)
    fun removeOnEffectValueChangeListener(listener: OnEffectValueChangeListener)

    fun interface OnEffectValueChangeListener {
        fun onEffectValueChange(effect: SimpleAudioEffect2, value: Int)
    }
}