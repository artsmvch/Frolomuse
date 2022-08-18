package com.frolo.audiofx2

sealed interface EqualizerPreset {
    val name: String
    val isDeletable: Boolean

    fun isTheSame(other: EqualizerPreset): Boolean

    interface Custom: EqualizerPreset

    interface Native: EqualizerPreset

    interface Saved: EqualizerPreset {
        val id: Long
        val numberOfBands: Int
        fun getBandLevel(band: Int): Int
    }
}