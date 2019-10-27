package com.frolo.muse.engine

import com.frolo.muse.model.preset.Preset


interface AudioFxObserver {
    fun onEnabled(audioFx: AudioFx)
    fun onDisabled(audioFx: AudioFx)
    fun onBandLevelChanged(audioFx: AudioFx, band: Short, level: Short)
    fun onPresetUsed(audioFx: AudioFx, preset: Preset)
    fun onBassStrengthChanged(audioFx: AudioFx, strength: Short)
    fun onVirtualizerStrengthChanged(audioFx: AudioFx, strength: Short)
    fun onPresetReverbUsed(audioFx: AudioFx, presetReverbIndex: Short)
}