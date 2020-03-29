package com.frolo.muse.engine;

import com.frolo.muse.model.preset.NativePreset;
import com.frolo.muse.model.preset.Preset;
import com.frolo.muse.model.reverb.Reverb;

import org.jetbrains.annotations.Nullable;

import java.util.List;


public interface AudioFx {
    /**
     * Save the current state of audio fx in own storage.
     */
    @Deprecated()
    void save();

    /**
     * Registers a new [observer].
     * This method has no effect if there is same observer registered.
     */
    void registerObserver(AudioFxObserver observer);
    /**
     * Unregisters [observer].
     * This method has no effect if the observer isn't registered.
     */
    void unregisterObserver(AudioFxObserver observer);

    /**
     * Returns true if the audio fx is initialized and has correct state, false - otherwise.
     */
    boolean isAvailable();
    /**
     * Returns true if the audio fx is enabled, false - otherwise.
     */
    boolean isEnabled();
    /**
     * Enables the audio fx if [enabled] is true, disables - otherwise.
     */
    void setEnabled(boolean enabled);

    /**
     * Returns true if the audio fx has equalizer, false - otherwise.
     */
    boolean hasEqualizer();
    /**
     * Returns the lower border of equalizer band range.
     * It will return 0 if the audio fx has no equalizer.
     */
    short getMinBandLevelRange();
    /**
     * Returns the higher border of equalizer band range.
     * It will return 0 if the audio fx has no equalizer.
     */
    short getMaxBandLevelRange();
    /**
     * Returns an array of frequency ranges for [band] in equalizer.
     * The size of array must be 2.
     * Implementation at discretion if the audio fx has no equalizer.
     */
    int[] getBandFreqRange(short band);
    /**
     * Returns number of bands in equalizer.
     * It will return 0 if the audio fx has no equalizer.
     */
    short getNumberOfBands();
    /**
     * Returns the level value of [band].
     * It will return 0 if the audio fx has no equalizer.
     */
    short getBandLevel(short band);
    /**
     * Sets the level value of [band] to [level].
     * It will have no effect if the audio fx has no equalizer.
     */
    void setBandLevel(short band, short level);

    /**
     * Returns list of available native presets.
     * @return list of available native presets.
     */
    List<NativePreset> getNativePresets();

    /**
     * Returns the preset that is being currently used in this AudioFx.
     * @return the current preset, or null if no preset is being used
     */
    @Nullable
    Preset getCurrentPreset();
    /**
     * Makes audio fx use the given <code>preset</code>.
     * @param preset to use
     */
    void usePreset(Preset preset);
    /**
     * Forces audio fx unuse preset.
     * It will have no effect if the audio fx has no equalizer.
     */
    void unusePreset();

    /**
     * Returns true if the audio fx has preset reverb effect, false - otherwise.
     */
    boolean hasPresetReverbEffect();

    /**
     * Returns list of available reverbs.
     * @return list of available reverbs.
     */
    List<Reverb> getReverbs();
    /**
     * Returns the reverb effect that is being currently used in this AudioFx.
     * @return the current reverb effect
     */
    Reverb getCurrentReverb();
    /**
     * Make this AudioFx use the given <code>reverb</code> effect.
     * @param reverb to use
     */
    void useReverb(Reverb reverb);

    /**
     * Returns true if the audio fx has bass boost effect, false - otherwise.
     */
    boolean hasBassBoost();
    /**
     * Returns the lower border of bass boost strength.
     * It will return 0 if the audio fx has no bass boost effect.
     */
    short getMinBassStrength();
    /**
     * Returns the higher border of bass boost strength.
     * It will return 0 if the audio fx has no bass boost effect.
     */
    short getMaxBassStrength();
    /**
     * Returns the current strength of bass boost effect.
     * It will return 0 if the audio fx has no bass boost effect.
     */
    short getBassStrength();
    /**
     * Sets the strength value of bass boost effect to [strength].
     * It will return 0 if the audio fx has no bass boost effect.
     */
    void setBassStrength(short strength);

    /**
     * Returns true if the audio fx has virtualizer effect, false - otherwise.
     */
    boolean hasVirtualizer();
    /**
     * Returns the lower border of virtualizer strength.
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    short getMinVirtualizerStrength();
    /**
     * Returns the higher border of virtualizer strength.
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    short getMaxVirtualizerStrength();
    /**
     * Returns the current strength of virtualizer strength.
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    short getVirtualizerStrength();
    /**
     * Sets the strength value of virtualizer effect to [strength].
     * It will return 0 if the audio fx has no virtualizer effect.
     */
    void setVirtualizerStrength(short strength);
}