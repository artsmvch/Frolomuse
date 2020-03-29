package com.frolo.muse.engine.stub

import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.AudioFxObserver
import com.frolo.muse.model.preset.NativePreset
import com.frolo.muse.model.preset.Preset
import com.frolo.muse.model.reverb.Reverb


object AudioFxStub: AudioFx {
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
    override fun getNativePresets(): List<NativePreset> = emptyList()
    override fun getCurrentPreset(): Preset? = null
    override fun unusePreset() = Unit
    override fun usePreset(preset: Preset?) = Unit
    override fun hasPresetReverbEffect(): Boolean = false
    override fun getReverbs(): List<Reverb> = emptyList()
    override fun getCurrentReverb(): Reverb = Reverb.NONE
    override fun useReverb(reverb: Reverb?) = Unit
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