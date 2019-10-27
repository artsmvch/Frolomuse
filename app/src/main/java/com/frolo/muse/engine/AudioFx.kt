package com.frolo.muse.engine

import com.frolo.muse.model.preset.CustomPreset
import com.frolo.muse.model.preset.NativePreset


interface AudioFx {
    /**
     * Save the current state of audio fx in own storage.
     */
    @Deprecated("Consider saving state after each mutation")
    fun save()

    /**
     * Registers a new [observer].
     * This method has no effect if there is same observer registered.
     */
    fun registerObserver(observer: AudioFxObserver)
    /**
     * Unregisters [observer].
     * This method has no effect if the observer isn't registered.
     */
    fun unregisterObserver(observer: AudioFxObserver)

    /**
     * Returns true if the audio fx is initialized and has correct state, false - otherwise.
     */
    fun isAvailable(): Boolean
    /**
     * Returns true if the audio fx is enabled, false - otherwise.
     */
    fun isEnabled(): Boolean
    /**
     * Enables the audio fx if [enabled] is true, disables - otherwise.
     */
    fun setEnabled(enabled: Boolean)

    /**
     * Returns true if the audio fx has equalizer, false - otherwise.
     */
    fun hasEqualizer(): Boolean
    /**
     * Returns the lower border of equalizer band range.
     * It will return 0 if the audio fx has no equalizer.
     */
    fun getMinBandLevelRange(): Short
    /**
     * Returns the higher border of equalizer band range.
     * It will return 0 if the audio fx has no equalizer.
     */
    fun getMaxBandLevelRange(): Short
    /**
     * Returns an array of frequency ranges for [band] in equalizer.
     * The size of array must be 2.
     * Implementation at discretion if the audio fx has no equalizer.
     */
    fun getBandFreqRange(band: Short): IntArray
    /**
     * Returns number of bands in equalizer.
     * It will return 0 if the audio fx has no equalizer.
     */
    fun getNumberOfBands(): Short
    /**
     * Returns the level value of [band].
     * It will return 0 if the audio fx has no equalizer.
     */
    fun getBandLevel(band: Short): Short
    /**
     * Sets the level value of [band] to [level].
     * It will have no effect if the audio fx has no equalizer.
     */
    fun setBandLevel(band: Short, level: Short)

    /**
     * Returns number of presets in equalizer.
     * It will return 0 if the audio fx has no equalizer.
     */
    fun getNumberOfPresets(): Short
    /**
     * Returns the name of band at [index] position in equalizer.
     * It will return empty string if the audio fx has no equalizer.
     */
    fun getPresetName(index: Short): String
    /**
     * Forces audio fx use native [preset].
     * It will have no effect if the audio fx has no equalizer.
     */
    fun useNativePreset(preset: NativePreset)
    /**
     * Forces audio fx unuse preset.
     * It will have no effect if the audio fx has no equalizer.
     */
    fun unusePreset()
    /**
     * Forces audio fx use custom [preset].
     * It will have no effect if the audio fx has no equalizer.
     */
    fun useCustomPreset(preset: CustomPreset)
    /**
     * Returns true if audio fx equalizer is using native preset.
     * It will return false if the audio fx is currently not using native preset or has no equalizer at all.
     */
    fun isUsingNativePreset(): Boolean
    /**
     * Returns true if audio fx equalizer is custom native preset.
     * It will return false if the audio fx is currently not using custom preset or has no equalizer at all.
     */
    fun isUsingCustomPreset(): Boolean
    /**
     * Returns native preset that the audio fx equalizer is currently using.
     * It will return null if the audio is currently not using any native preset or has no equalizer at all.
     */
    fun getCurrentNativePreset(): NativePreset?
    /**
     * Returns custom preset that the audio fx equalizer is currently using.
     * It will return null if the audio is currently not using any custom preset or has no equalizer at all.
     */
    fun getCurrentCustomPreset(): CustomPreset?
    /**
     * Returns true if the audio fx has preset reverb effect, false - otherwise.
     */
    fun hasPresetReverb(): Boolean
    /**
     * Returns number of available preset reverbs.
     * It will return 0 if the audio fx has no preset reverb effect.
     */
    fun getNumberOfPresetReverbs(): Short
    /**
     * Returns indexes of available preset reverbs.
     * It will return an empty array if the audio fx has no preset reverb effect.
     */
    fun getPresetReverbIndexes(): ShortArray
    /**
     * Returns the name of preset reverb at [index] position.
     * It will return empty string if the audio fx has no preset reverb effect.
     */
    fun getPresetReverbName(index: Short): String
    /**
     * Force the audio fx preset reverb effect use the preset at [index] position.
     * It will have no effect if the audio fx has no preset reverb effect.
     */
    fun usePresetReverb(index: Short)
    /**
     * Returns index of preset reverb that the audio fx preset reverb effect is currently using.
     * It will return -1 if the audio fx has no preset reverb effect.
     */
    fun getCurrentPresetReverb(): Short

    /**
     * Returns true if the audio fx has bass boost effect, false - otherwise.
     */
    fun hasBassBoost(): Boolean
    /**
     * Returns the lower border of bass boost strength.
     * It will return 0 if the audio fx has no bass boost effect.
     */
    fun getMinBassStrength(): Short
    /**
     * Returns the higher border of bass boost strength.
     * It will return 0 if the audio fx has no bass boost effect.
     */
    fun getMaxBassStrength(): Short
    /**
     * Returns the current strength of bass boost effect.
     * It will return 0 if the audio fx has no bass boost effect.
     */
    fun getBassStrength(): Short
    /**
     * Sets the strength value of bass boost effect to [strength].
     * It will return 0 if the audio fx has no bass boost effect.
     */
    fun setBassStrength(strength: Short)

    /**
     * Returns true if the audio fx has virtualizer effect, false - otherwise.
     */
    fun hasVirtualizer(): Boolean
    /**
     * Returns the lower border of virtualizer strength.
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    fun getMinVirtualizerStrength(): Short
    /**
     * Returns the higher border of virtualizer strength.
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    fun getMaxVirtualizerStrength(): Short
    /**
     * Returns the current strength of virtualizer strength.
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    fun getVirtualizerStrength(): Short
    /**
     * Sets the strength value of virtualizer effect to [strength].
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    fun setVirtualizerStrength(strength: Short)
}