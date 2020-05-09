package com.frolo.muse.engine.audiofx;

import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.util.Log;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.engine.AudioFxApplicable;
import com.frolo.muse.engine.AudioFxObserver;
import com.frolo.muse.model.preset.CustomPreset;
import com.frolo.muse.model.preset.NativePreset;
import com.frolo.muse.model.preset.Preset;
import com.frolo.muse.model.reverb.Reverb;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link AudioFxApplicable} based on the AudioFx effects from the Android SDK.
 */
public class AudioFx_Impl implements AudioFxApplicable {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static AudioFx_Impl getInstance(Context context, String prefsName) {
        return new AudioFx_Impl(context, prefsName);
    }

    private static short clamp(short minValue, short maxValue, short value) {
        if (value < minValue) return minValue;
        if (value > maxValue) return maxValue;
        return value;
    }

    private final Context mContext;

    /**
     * Remember the last session ID so we can compare it later when {@link AudioFxApplicable#apply(int)} method gets called.
     * If the audio session ID doesn't change then we can omit AudioFx adjustment to that session ID.
     */
    private volatile Integer mLastSessionId = null;

    private volatile Equalizer mEqualizer;
    private volatile BassBoost mBassBoost;
    private volatile Virtualizer mVirtualizer;
    private volatile PresetReverb mPresetReverb;

    private final boolean mHasEqualizer;
    private final boolean mHasBassBoost;
    private final boolean mHasVirtualizer;
    private final boolean mHasPresetReverb;

    private final AudioFx_Persistence mPersistence;

    private final AudioFx_ObserverRegistry mObserverRegistry;

    AudioFx_Impl(Context context, String prefsName) {
        mContext = context;

        // Checking what audio effects the device does have
        boolean hasEqualizer = false;
        boolean hasBassBoost = false;
        boolean hasVirtualizer = false;
        boolean hasPresetReverb = false;
        try {
            final AudioEffect.Descriptor[] descriptors = AudioEffect.queryEffects();

            if (descriptors != null) {
                for (AudioEffect.Descriptor descriptor : descriptors) {
                    if (Objects.equals(AudioEffect.EFFECT_TYPE_EQUALIZER, descriptor.type))
                        hasEqualizer = true;
                    if (Objects.equals(AudioEffect.EFFECT_TYPE_BASS_BOOST, descriptor.type))
                        hasBassBoost = true;
                    if (Objects.equals(AudioEffect.EFFECT_TYPE_VIRTUALIZER, descriptor.type))
                        hasVirtualizer = true;
                    if (Objects.equals(AudioEffect.EFFECT_TYPE_PRESET_REVERB, descriptor.type))
                        hasPresetReverb = true;
                }
            }
        } catch (Throwable t) {
            report(t);
        }

        mHasEqualizer = hasEqualizer;
        mHasBassBoost = hasBassBoost;
        mHasVirtualizer = hasVirtualizer;
        mHasPresetReverb = hasPresetReverb;

        mPersistence = AudioFx_Persistence.create(context, prefsName);

        mObserverRegistry = AudioFx_ObserverRegistry.create(context, this);
    }

    private void report(Throwable t) {
        Log.e("AudioFx_Impl", "A critical error occurred", t);
    }

    @Override
    public void save() {
        mPersistence.save();
    }

    @Override
    public void registerObserver(AudioFxObserver observer) {
        mObserverRegistry.register(observer);
    }

    @Override
    public void unregisterObserver(AudioFxObserver observer) {
        mObserverRegistry.unregister(observer);
    }

    @Override
    public boolean isAvailable() {
        return mLastSessionId != null;
    }

    @Override
    public boolean isEnabled() {
        return mPersistence.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        mPersistence.saveEnabled(enabled);

        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                equalizer.setEnabled(enabled);
        } catch (Throwable t) {
            report(t);
        }

        try {
            BassBoost bassBoost = mBassBoost;
            if (bassBoost != null)
                bassBoost.setEnabled(enabled);
        } catch (Throwable t) {
            report(t);
        }

        try {
            Virtualizer virtualizer = mVirtualizer;
            if (virtualizer != null)
                virtualizer.setEnabled(enabled);
        } catch (Throwable t) {
            report(t);
        }

        try {
            PresetReverb presetReverb = mPresetReverb;
            if (presetReverb != null)
                presetReverb.setEnabled(enabled);
        } catch (Throwable t) {
            report(t);
        }

        if (enabled) {
            mObserverRegistry.dispatchEnabled();
        } else {
            mObserverRegistry.dispatchDisabled();
        }
    }

    @Override
    public boolean hasEqualizer() {
        return mHasEqualizer;
    }

    @Override
    public short getMinBandLevelRange() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                return equalizer.getBandLevelRange()[0];
            return 0;
        } catch (Throwable t) {
            report(t);
            return 0;
        }
    }

    @Override
    public short getMaxBandLevelRange() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                return equalizer.getBandLevelRange()[1];
            return 0;
        } catch (Throwable t) {
            report(t);
            return 0;
        }
    }

    @Override
    public int[] getBandFreqRange(short band) {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                return equalizer.getBandFreqRange(band);
            return getDefaultBandFreqRange(band);
        } catch (Throwable t) {
            report(t);
            return getDefaultBandFreqRange(band);
        }
    }

    private int[] getDefaultBandFreqRange(short band) {
        if (band == 0)
            return new int[] { 30_000, 120_000 };
        if (band == 1)
            return new int[] { 120_001, 460_000 };
        if (band == 2)
            return new int[] { 460_001, 1_800_00 };
        if (band == 3)
            return new int[] { 1_800_001, 7_000_000 };
        if (band == 4)
            return new int[] { 7_000_000, 20_000_000 };

        return new int[] { 7_000_000, 20_000_000 };
    }

    @Override
    public short getNumberOfBands() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                return equalizer.getNumberOfBands();
            return 0;
        } catch (Throwable t) {
            report(t);
            return 0;
        }
    }

    @Override
    public short getBandLevel(short band) {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                return equalizer.getBandLevel(band);
            return 0;
        } catch (Throwable t) {
            report(t);
            return 0;
        }
    }

    @Override
    public void setBandLevel(short band, short level) {
        try {
            mPersistence.saveBandLevel(band, level);

            Equalizer equalizer = mEqualizer;
            if (equalizer != null)
                equalizer.setBandLevel(band, level);
        } catch (Throwable t) {
            report(t);
        } finally {
            mObserverRegistry.dispatchBandLevelChanged(band, level);
        }
    }

    @Override
    public List<NativePreset> getNativePresets() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer == null)
                return new ArrayList<>(0);

            final int numberOfPresets = equalizer.getNumberOfPresets();
            final List<NativePreset> presets = new ArrayList<>(numberOfPresets);
            for (int i = 0; i < numberOfPresets; i++) {
                short presetIndex = (short) i;
                NativePreset preset =
                        new NativePreset(presetIndex, equalizer.getPresetName(presetIndex));
                presets.add(preset);
            }
            return presets;
        } catch (Throwable t) {
            report(t);
            return new ArrayList<>(0);
        }
    }

    @Nullable
    @Override
    public Preset getCurrentPreset() {
        final int eqUseFlag = mPersistence.getEqUseFlag();
        if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_NATIVE_PRESET) {
            return mPersistence.getLastNativePreset();
        } else if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_CUSTOM_PRESET) {
            return mPersistence.getLastCustomPreset();
        } else {
            // no preset
            return null;
        }
    }

    @Override
    public void usePreset(Preset preset) {
        if (preset instanceof NativePreset) {
            int eqUseFlag = mPersistence.getEqUseFlag();
            NativePreset lastPreset = mPersistence.getLastNativePreset();
            if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_NATIVE_PRESET
                    && Objects.equals(preset, lastPreset)) {
                // No changes
                return;
            }

            try {
                Equalizer equalizer = mEqualizer;
                if (equalizer != null) {
                    short presetIndex = ((NativePreset) preset).getIndex();
                    equalizer.usePreset(presetIndex);

                    int numberOfBands = equalizer.getNumberOfBands();
                    for (short band = 0; band < numberOfBands; band++) {
                        final short level = equalizer.getBandLevel(band);
                        mPersistence.saveBandLevel(band, level);
                        mObserverRegistry.dispatchBandLevelChanged(band, level);
                    }
                }
            } catch (Throwable t) {
                report(t);
            }

            mPersistence.saveEqUseFlag(AudioFx_Persistence.FLAG_EQ_USE_NATIVE_PRESET);
            mPersistence.saveLastNativePreset((NativePreset) preset);
            mObserverRegistry.dispatchPresetUsed(preset);
        } else if (preset instanceof CustomPreset) {

            int eqUseFlag = mPersistence.getEqUseFlag();
            CustomPreset lastPreset = mPersistence.getLastCustomPreset();
            if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_CUSTOM_PRESET
                    && Objects.equals(preset, lastPreset)) {
                // No changes
                return;
            }

            try {
                Equalizer equalizer = mEqualizer;
                if (equalizer != null) {
                    short[] levels = ((CustomPreset) preset).getLevels();

                    int numberOfBands = equalizer.getNumberOfBands();
                    int levelCount = levels != null ? levels.length : 0;
                    for (short band = 0; band < Math.min(numberOfBands, levelCount); band++) {
                        final short level = levels[band];
                        equalizer.setBandLevel(band, level);
                        mPersistence.saveBandLevel(band, level);
                        mObserverRegistry.dispatchBandLevelChanged(band, level);
                    }
                }
            } catch (Throwable t) {
                report(t);
            }

            mPersistence.saveEqUseFlag(AudioFx_Persistence.FLAG_EQ_USE_CUSTOM_PRESET);
            mPersistence.saveLastCustomPreset((CustomPreset) preset);
            mObserverRegistry.dispatchPresetUsed(preset);
        } else {
            // unknown preset, no action required
        }
    }

    @Override
    public void unusePreset() {
        mPersistence.saveEqUseFlag(AudioFx_Persistence.FLAG_EQ_USE_NO_PRESET);
    }

    @Override
    public boolean hasPresetReverbEffect() {
        return mHasPresetReverb;
    }

    @Override
    public List<Reverb> getReverbs() {
        return Arrays.asList(Reverb.values());
    }

    @Override
    public Reverb getCurrentReverb() {
        try {
            PresetReverb presetReverb = mPresetReverb;
            if (presetReverb == null)
                return Reverb.NONE;

            final short presetReverbIndex = presetReverb.getPreset();
            if (presetReverbIndex == PresetReverb.PRESET_LARGEHALL)
                return Reverb.LARGE_HALL;
            if (presetReverbIndex == PresetReverb.PRESET_LARGEROOM)
                return Reverb.LARGE_ROOM;
            if (presetReverbIndex == PresetReverb.PRESET_MEDIUMHALL)
                return Reverb.MEDIUM_HALL;
            if (presetReverbIndex == PresetReverb.PRESET_MEDIUMROOM)
                return Reverb.MEDIUM_ROOM;
            if (presetReverbIndex == PresetReverb.PRESET_PLATE)
                return Reverb.PLATE;
            if (presetReverbIndex == PresetReverb.PRESET_SMALLROOM)
                return Reverb.SMALL_ROOM;
            return Reverb.NONE;
        } catch (Throwable t) {
            report(t);
            return Reverb.NONE;
        }
    }

    @Override
    public void useReverb(Reverb reverb) {
        mPersistence.saveReverb(reverb);

        try {
            PresetReverb presetReverb = mPresetReverb;
            if (presetReverb != null) {
                final short presetIndex = getPresetReverbIndex(reverb);
                presetReverb.setPreset(presetIndex);
            }
        } catch (Throwable t) {
            report(t);
        } finally {
            mObserverRegistry.dispatchReverbUsed(reverb);
        }
    }

    private short getPresetReverbIndex(Reverb reverb) {
        switch (reverb) {
            case LARGE_HALL:
                return PresetReverb.PRESET_LARGEHALL;
            case LARGE_ROOM:
                return PresetReverb.PRESET_LARGEROOM;
            case MEDIUM_HALL:
                return PresetReverb.PRESET_MEDIUMHALL;
            case MEDIUM_ROOM:
                return PresetReverb.PRESET_MEDIUMROOM;
            case PLATE:
                return PresetReverb.PRESET_PLATE;
            case SMALL_ROOM:
                return PresetReverb.PRESET_SMALLROOM;
            default:
                return PresetReverb.PRESET_NONE;
        }
    }

    @Override
    public boolean hasBassBoost() {
        return mHasBassBoost;
    }

    @Override
    public short getMinBassStrength() {
        return 0;
    }

    @Override
    public short getMaxBassStrength() {
        return 999;
    }

    @Override
    public short getBassStrength() {
        try {
            BassBoost bassBoost = mBassBoost;
            if (bassBoost != null)
                return bassBoost.getRoundedStrength();
            return 0;
        } catch (Throwable t) {
            report(t);
            return 0;
        }
    }

    @Override
    public void setBassStrength(short strength) {
        // validating
        strength = clamp(getMinBassStrength(), getMaxBassStrength(), strength);
        mPersistence.saveBassStrength(strength);
        try {
            BassBoost bassBoost = mBassBoost;
            if (bassBoost != null)
                bassBoost.setStrength(strength);
        } catch (Throwable t) {
            report(t);
        } finally {
            mObserverRegistry.dispatchBassStrengthChanged(strength);
        }
    }

    @Override
    public boolean hasVirtualizer() {
        return mHasVirtualizer;
    }

    @Override
    public short getMinVirtualizerStrength() {
        return 0;
    }

    @Override
    public short getMaxVirtualizerStrength() {
        return 999;
    }

    @Override
    public short getVirtualizerStrength() {
        try {
            Virtualizer virtualizer = mVirtualizer;
            if (virtualizer != null)
                return virtualizer.getRoundedStrength();
            return 0;
        } catch (Throwable t) {
            report(t);
            return 0;
        }
    }

    @Override
    public void setVirtualizerStrength(short strength) {
        // validating
        strength = clamp(getMinVirtualizerStrength(), getMaxVirtualizerStrength(), strength);
        mPersistence.saveVirtualizerStrength(strength);
        try {
            Virtualizer virtualizer = mVirtualizer;
            if (virtualizer != null)
                virtualizer.setStrength(strength);
        } catch (Throwable t) {
            report(t);
        } finally {
            mObserverRegistry.dispatchVirtualizerStrengthChanged(strength);
        }
    }

    @Override
    public synchronized void apply(int audioSessionId) {
        final Integer currSessionId = mLastSessionId;

        // If it doesn't change, then we can omit adjustment
        final boolean sessionHasChanged = currSessionId == null || currSessionId != audioSessionId;

        final int priority = 0;

        // Re-set equalizer, if needed
        if (sessionHasChanged || (mHasEqualizer && mEqualizer == null)) {
            try {
                Equalizer oldEqualizer = mEqualizer;
                if (oldEqualizer != null)
                    oldEqualizer.release();
            } catch (Throwable t) {
                report(t);
            }

            try {
                Equalizer newEqualizer = new Equalizer(priority, audioSessionId);

                mEqualizer = newEqualizer;

                newEqualizer.setEnabled(mPersistence.isEnabled());

                final int eqUseFlag = mPersistence.getEqUseFlag();
                final boolean adjustBandLevels;
                if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_NO_PRESET) {
                    adjustBandLevels = true;
                } else if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_NATIVE_PRESET) {
                    NativePreset preset = mPersistence.getLastNativePreset();
                    if (preset != null) {
                        newEqualizer.usePreset(preset.getIndex());
                    }
                    adjustBandLevels = preset == null;
                } else if (eqUseFlag == AudioFx_Persistence.FLAG_EQ_USE_CUSTOM_PRESET) {
                    CustomPreset preset = mPersistence.getLastCustomPreset();
                    short[] bandLevels = preset != null ? preset.getLevels() : null;
                    if (bandLevels != null) {
                        for (int i = 0; i < bandLevels.length; i++) {
                            newEqualizer.setBandLevel((short) i, bandLevels[i]);
                        }
                    }
                    adjustBandLevels = bandLevels == null;
                } else {
                    adjustBandLevels = true;
                }

                if (adjustBandLevels) {
                    short[] bandLevels = mPersistence.getLastBandLevels();
                    for (int i = 0; i < bandLevels.length; i++) {
                        newEqualizer.setBandLevel((short) i, bandLevels[i]);
                    }
                }
            } catch (Throwable t) {
                report(t);
            }
        }

        // Re-set bass boost, if needed
        if (sessionHasChanged || (mHasBassBoost && mBassBoost == null)) {
            try {
                BassBoost oldBassBoost = mBassBoost;
                if (oldBassBoost != null)
                    oldBassBoost.release();
            } catch (Throwable t) {
                report(t);
            }

            try {
                BassBoost newBassBoost = new BassBoost(priority, audioSessionId);

                mBassBoost = newBassBoost;

                newBassBoost.setEnabled(mPersistence.isEnabled());
                newBassBoost.setStrength(mPersistence.getBassStrength());
            } catch (Throwable t) {
                report(t);
            }
        }

        // Re-set virtualizer, if needed
        if (sessionHasChanged || (mHasVirtualizer && mVirtualizer == null)) {
            try {
                Virtualizer oldVirtualizer = mVirtualizer;
                if (oldVirtualizer != null)
                    oldVirtualizer.release();
            } catch (Throwable t) {
                report(t);
            }

            try {
                Virtualizer newVirtualizer = new Virtualizer(priority, audioSessionId);

                mVirtualizer = newVirtualizer;

                newVirtualizer.setEnabled(mPersistence.isEnabled());
                newVirtualizer.setStrength(mPersistence.getVirtualizerStrength());
            } catch (Throwable t) {
                report(t);
            }
        }

        // Re-set preset reverb, if needed
        if (sessionHasChanged || (mHasPresetReverb && mPresetReverb == null)) {
            try {
                PresetReverb oldPresetReverb = mPresetReverb;
                if (oldPresetReverb != null)
                    oldPresetReverb.release();
            } catch (Throwable t) {
                report(t);
            }

            try {
                PresetReverb newPresetReverb = new PresetReverb(priority, audioSessionId);

                mPresetReverb = newPresetReverb;

                newPresetReverb.setEnabled(mPersistence.isEnabled());

                final short presetReverbIndex = getPresetReverbIndex(mPersistence.getReverb());
                newPresetReverb.setPreset(presetReverbIndex);
            } catch (Throwable t) {
                report(t);
            }
        }

        mLastSessionId = audioSessionId;
    }
}
