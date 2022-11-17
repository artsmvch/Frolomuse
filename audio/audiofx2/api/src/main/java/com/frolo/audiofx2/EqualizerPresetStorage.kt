package com.frolo.audiofx2

interface EqualizerPresetStorage {
    fun getCurrentPreset(): EqualizerPreset

    fun getAllPresets(): List<EqualizerPreset>

    fun createPreset(name: String, bandLevels: Map<Int, Int>): EqualizerPreset

    fun deletePreset(preset: EqualizerPreset)

    fun addOnStorageUpdateListener(listener: OnStorageUpdateListener)
    fun removeOnStorageUpdateListener(listener: OnStorageUpdateListener)

    interface OnStorageUpdateListener{
        fun onPresetCreated(storage: EqualizerPresetStorage, preset: EqualizerPreset)
        fun onPresetDeleted(storage: EqualizerPresetStorage, preset: EqualizerPreset)
    }
}