package com.frolo.audiofx2

interface Reverb: AudioEffect2 {
    val availablePresets: List<Preset>
    var preset: Preset?

    interface Preset {
        val name: String
    }
}