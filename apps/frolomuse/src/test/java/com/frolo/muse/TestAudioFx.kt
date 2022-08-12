package com.frolo.muse

import com.frolo.audiofx.AudioFx
import com.frolo.audiofx.AudioFxObserver
import com.frolo.audiofx.NativePreset
import com.frolo.audiofx.Preset
import com.frolo.audiofx.Reverb


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
    override fun getNativePresets(): MutableList<NativePreset> = mutableListOf()
    override fun usePreset(preset: Preset?) = Unit
    override fun getReverbs(): MutableList<Reverb> = mutableListOf()
    override fun useReverb(reverb: Reverb?) = Unit
    override fun getCurrentPreset(): Preset? = null
    override fun unusePreset() = Unit
    override fun hasPresetReverbEffect(): Boolean = false
    override fun getCurrentReverb(): Reverb = Reverb.NONE
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