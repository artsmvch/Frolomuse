package com.frolo.audiofx2

interface EqualizerPresetStorage {
    fun getCurrentPreset(): EqualizerPreset

    fun getAllPresets(): List<EqualizerPreset>

    fun createPreset(name: String, bandLevels: Map<Int, Int>): EqualizerPreset

    fun deletePreset(preset: EqualizerPreset)
}