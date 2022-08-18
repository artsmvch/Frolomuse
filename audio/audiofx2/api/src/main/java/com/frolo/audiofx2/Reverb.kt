package com.frolo.audiofx2

interface Reverb: AudioEffect2 {
    val availablePresets: List<Preset>
    var preset: Preset

    fun addOnPresetUsedListener(listener: OnPresetUsedListener)
    fun removeOnPresetUsedListener(listener: OnPresetUsedListener)

    interface Preset {
        val level: Int
        val name: String

        fun isTheSame(other: Preset): Boolean
    }

    fun interface OnPresetUsedListener {
        fun onPresetUsed(effect: Reverb, preset: Preset)
    }
}