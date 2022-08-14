package com.frolo.audiofx2

sealed interface EqualizerPreset {
    val name: String
    val isDeletable: Boolean

    interface Native: EqualizerPreset

    interface Custom: EqualizerPreset {
        val id: Long
        val numberOfBands: Int
        fun getBandLevel(band: Int): Int
    }
}