package com.frolo.muse

import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.AudioFxObserver
import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.NativePreset


class TestAudioFx: AudioFx {
    override fun save() = Unit
    override fun registerObserver(observer: AudioFxObserver) = Unit
    override fun unregisterObserver(observer: AudioFxObserver) = Unit
    override fun isAvailable(): Boolean = false
    override fun isEnabled(): Boolean = false
    override fun setEnabled(enabled: Boolean) = Unit
    override fun hasEqualizer(): Boolean = false
    override fun getMinBandLevelRange(): Short = 0
    override fun getMaxBandLevelRange(): Short = 0
    override fun getBandFreqRange(band: Short): IntArray = intArrayOf()
    override fun getNumberOfBands(): Short = 0
    override fun getBandLevel(band: Short): Short = 0
    override fun setBandLevel(band: Short, level: Short) = Unit
    override fun getNumberOfPresets(): Short = 0
    override fun getPresetName(index: Short): String = ""
    override fun useNativePreset(preset: NativePreset) = Unit
    override fun unusePreset() = Unit
    override fun useCustomPreset(preset: CustomPreset) = Unit
    override fun isUsingNativePreset(): Boolean = false
    override fun isUsingCustomPreset(): Boolean = false
    override fun getCurrentNativePreset(): NativePreset? = null
    override fun getCurrentCustomPreset(): CustomPreset? = null
    override fun hasPresetReverb(): Boolean = false
    override fun getNumberOfPresetReverbs(): Short = 0
    override fun getPresetReverbIndexes(): ShortArray = shortArrayOf()
    override fun getPresetReverbName(index: Short): String = ""
    override fun usePresetReverb(index: Short) = Unit
    override fun getCurrentPresetReverb(): Short = 0
    override fun hasBassBoost(): Boolean = false
    override fun getMinBassStrength(): Short = 0
    override fun getMaxBassStrength(): Short = 0
    override fun getBassStrength(): Short = 0
    override fun setBassStrength(strength: Short) = Unit
    override fun hasVirtualizer(): Boolean = false
    override fun getMinVirtualizerStrength(): Short = 0
    override fun getMaxVirtualizerStrength(): Short = 0
    override fun getVirtualizerStrength(): Short = 0
    override fun setVirtualizerStrength(strength: Short) = Unit
}