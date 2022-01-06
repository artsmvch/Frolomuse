package com.frolo.audiofx;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.util.Log;

import androidx.annotation.NonNull;

import com.frolo.audiofx.applicable.AudioFxApplicable;
import com.frolo.vendor.ManufacturerUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * Implementation of {@link AudioFxApplicable} based on the AudioFx effects from the Android SDK.
 */
public class AudioFxImpl implements AudioFxApplicable {

    private static final String LOG_TAG = "AudioFx_Impl";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * If this flag is set to true, then, when {@link AudioFxApplicable#apply(MediaPlayer)} is called,
     * the AudioFx can omit the initialization of all audio effects if the audio session does NOT change.
     *
     * If this flag is set to false, then the AudioFx initialize all audio effects
     * every time {@link AudioFxApplicable#apply(MediaPlayer)} is called.
     *
     * Be careful by settings this to false, because it may be not an optimal solution.
     */
    private static final boolean OPTIMIZE_AUDIO_SESSION_CHANGE = true;

    public interface ErrorHandler {
        void onError(Throwable error);
    }

    public static AudioFxImpl getInstance(@NotNull Context context, @NotNull String prefsName) {
        return getInstance(context, prefsName, null);
    }

    public static AudioFxImpl getInstance(
            @NotNull Context context, @NotNull String prefsName, @Nullable ErrorHandler errorHandler) {
        return new AudioFxImpl(context, prefsName, errorHandler);
    }

    private static short clamp(short minValue, short maxValue, short value) {
        if (value < minValue) return minValue;
        if (value > maxValue) return maxValue;
        return value;
    }

    private final Context mContext;

    /**
     * Remember the last session ID so we can compare it later when {@link AudioFxApplicable#apply(MediaPlayer)} method gets called.
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

    private final AudioFxPersistence mPersistence;

    private final AudioFxObserverRegistry mObserverRegistry;

    private final ErrorHandler mErrorHandler;

    AudioFxImpl(Context context, String prefsName, ErrorHandler errorHandler) {
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

        mPersistence = AudioFxPersistence.create(context, prefsName);

        mObserverRegistry = AudioFxObserverRegistry.create(context, this);

        mErrorHandler = errorHandler;

        if (DEBUG) {
            String msg = new StringBuilder("Initialized:\n")
                .append("hasEqualizer=").append(mHasEqualizer).append("\n")
                .append("hasBassBoost=").append(mHasBassBoost).append("\n")
                .append("hasVirtualizer=").append(mHasVirtualizer).append("\n")
                .append("hasPresetReverb=").append(mHasPresetReverb).append("\n")
                .toString();
            Log.d(LOG_TAG, msg);
        }
    }

    private void report(Throwable t) {
        AudioFxException error = new AudioFxException(t);
        if (DEBUG) Log.e(LOG_TAG, "A critical error occurred", error);
        if (mErrorHandler != null) {
            mErrorHandler.onError(error);
        }
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
            if (equalizer != null) {
                equalizer.setEnabled(enabled);
            }
        } catch (Throwable t) {
            report(t);
        }

        try {
            BassBoost bassBoost = mBassBoost;
            if (bassBoost != null) {
                bassBoost.setEnabled(enabled);
            }
        } catch (Throwable t) {
            report(t);
        }

        try {
            Virtualizer virtualizer = mVirtualizer;
            if (virtualizer != null) {
                virtualizer.setEnabled(enabled);
            }
        } catch (Throwable t) {
            report(t);
        }

        try {
            PresetReverb presetReverb = mPresetReverb;
            if (presetReverb != null) {
                presetReverb.setEnabled(enabled);
            }
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
            if (equalizer != null) {
                return equalizer.getBandLevelRange()[0];
            }
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
            if (equalizer != null) {
                return equalizer.getBandLevelRange()[1];
            }
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
            if (equalizer != null) {
                return equalizer.getBandFreqRange(band);
            }
            return getDefaultBandFreqRange(band);
        } catch (Throwable t) {
            report(t);
            return getDefaultBandFreqRange(band);
        }
    }

    private int[] getDefaultBandFreqRange(short band) {
        switch (band) {
            case 0:
                return new int[]{30_000, 120_000};
            case 1:
                return new int[]{120_001, 460_000};
            case 2:
                return new int[]{460_001, 1_800_00};
            case 3:
                return new int[]{1_800_001, 7_000_000};
            default:
                return new int[]{7_000_000, 20_000_000};
        }
    }

    @Override
    public short getNumberOfBands() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null) {
                return equalizer.getNumberOfBands();
            }
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
            if (equalizer != null) {
                return equalizer.getBandLevel(band);
            }
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
            if (equalizer != null) {
                equalizer.setBandLevel(band, level);
            }
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
            if (equalizer == null) {
                return new ArrayList<>(0);
            }

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
        if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_NATIVE_PRESET) {
            return mPersistence.getLastNativePreset();
        } else if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_CUSTOM_PRESET) {
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
            if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_NATIVE_PRESET
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

            mPersistence.saveEqUseFlag(AudioFxPersistence.FLAG_EQ_USE_NATIVE_PRESET);
            mPersistence.saveLastNativePreset((NativePreset) preset);
            mObserverRegistry.dispatchPresetUsed(preset);
        } else if (preset instanceof CustomPreset) {

            int eqUseFlag = mPersistence.getEqUseFlag();
            CustomPreset lastPreset = mPersistence.getLastCustomPreset();
            if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_CUSTOM_PRESET
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

            mPersistence.saveEqUseFlag(AudioFxPersistence.FLAG_EQ_USE_CUSTOM_PRESET);
            mPersistence.saveLastCustomPreset((CustomPreset) preset);
            mObserverRegistry.dispatchPresetUsed(preset);
        } else {
            // unknown preset, no action required
        }
    }

    @Override
    public void unusePreset() {
        mPersistence.saveEqUseFlag(AudioFxPersistence.FLAG_EQ_USE_NO_PRESET);
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
            if (presetReverb == null) {
                return Reverb.NONE;
            }

            final short presetReverbIndex = presetReverb.getPreset();
            switch (presetReverbIndex) {
                case PresetReverb.PRESET_LARGEHALL:
                    return Reverb.LARGE_HALL;
                case PresetReverb.PRESET_LARGEROOM:
                    return Reverb.LARGE_ROOM;
                case PresetReverb.PRESET_MEDIUMHALL:
                    return Reverb.MEDIUM_HALL;
                case PresetReverb.PRESET_MEDIUMROOM:
                    return Reverb.MEDIUM_ROOM;
                case PresetReverb.PRESET_PLATE:
                    return Reverb.PLATE;
                case PresetReverb.PRESET_SMALLROOM:
                    return Reverb.SMALL_ROOM;
                default:
                    return Reverb.NONE;
            }
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
            if (bassBoost != null) {
                return bassBoost.getRoundedStrength();
            }
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
            if (bassBoost != null) {
                bassBoost.setStrength(strength);
            }
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
            if (virtualizer != null) {
                return virtualizer.getRoundedStrength();
            }
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
            if (virtualizer != null) {
                virtualizer.setStrength(strength);
            }
        } catch (Throwable t) {
            report(t);
        } finally {
            mObserverRegistry.dispatchVirtualizerStrengthChanged(strength);
        }
    }

    @Override
    public synchronized void apply(@NonNull MediaPlayer engine) {
        final int audioSessionId = engine.getAudioSessionId();

        final Integer currSessionId = mLastSessionId;

        final boolean sessionHasChanged = currSessionId == null || currSessionId != audioSessionId;

        // We can omit the initialization if and only if the optimization is enabled and the session has not changed
        final boolean canOmitInitialization = OPTIMIZE_AUDIO_SESSION_CHANGE && !sessionHasChanged;

        final int priority = 0;

        final boolean enabled = mPersistence.isEnabled();

        if (DEBUG) {
            String msg = new StringBuilder("Apply: \n")
                .append("audioSessionId=").append(audioSessionId).append("\n")
                .append("sessionHasChanged=").append(sessionHasChanged).append("\n")
                .append("canOmitInitialization=").append(canOmitInitialization).append("\n")
                .append("priority=").append(priority).append("\n")
                .append("enabled=").append(enabled).append("\n")
                .toString();
            Log.d(LOG_TAG, msg);
        }

        // Re-set equalizer, if needed
        if (mHasEqualizer && (!canOmitInitialization || mEqualizer == null)) {
            try {
                Equalizer oldEqualizer = mEqualizer;
                if (oldEqualizer != null) {
                    oldEqualizer.release();
                }
            } catch (Throwable t) {
                report(t);
            }

            try {
                Equalizer newEqualizer = new Equalizer(priority, audioSessionId);

                mEqualizer = newEqualizer;

                final int eqUseFlag = mPersistence.getEqUseFlag();
                final boolean adjustBandLevels;
                if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_NO_PRESET) {
                    adjustBandLevels = true;
                } else if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_NATIVE_PRESET) {
                    NativePreset preset = mPersistence.getLastNativePreset();
                    if (preset != null) {
                        newEqualizer.usePreset(preset.getIndex());
                    }
                    adjustBandLevels = preset == null;
                } else if (eqUseFlag == AudioFxPersistence.FLAG_EQ_USE_CUSTOM_PRESET) {
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

                newEqualizer.setEnabled(enabled);
            } catch (Throwable t) {
                report(t);
            }
        }

        // Re-set bass boost, if needed
        if (mHasBassBoost && (!canOmitInitialization || mBassBoost == null)) {
            try {
                BassBoost oldBassBoost = mBassBoost;
                if (oldBassBoost != null) {
                    oldBassBoost.release();
                }
            } catch (Throwable t) {
                report(t);
            }

            try {
                BassBoost newBassBoost = new BassBoost(priority, audioSessionId);

                mBassBoost = newBassBoost;

                newBassBoost.setStrength(mPersistence.getBassStrength());
                newBassBoost.setEnabled(enabled);
            } catch (Throwable t) {
                report(t);
            }
        }

        // Re-set virtualizer, if needed
        if (mHasVirtualizer && (!canOmitInitialization || mVirtualizer == null)) {
            try {
                Virtualizer oldVirtualizer = mVirtualizer;
                if (oldVirtualizer != null) {
                    oldVirtualizer.release();
                }
            } catch (Throwable t) {
                report(t);
            }

            try {
                Virtualizer newVirtualizer = new Virtualizer(priority, audioSessionId);

                mVirtualizer = newVirtualizer;

                newVirtualizer.setStrength(mPersistence.getVirtualizerStrength());
                newVirtualizer.setEnabled(enabled);
            } catch (Throwable t) {
                report(t);
            }
        }

        // In this method, we always set up the preset reverb (if the device has such an audio effect).
        // Always, because it is applied directly to the media player and not to its audio session ID.
        // NOTE: For Xiaomi devices, the preset reverb is applied directly to the audio session ID.
        // In any case, we'd better re-setup it (release the old, create a new one).
        if (mHasPresetReverb) {
            try {
                PresetReverb oldPresetReverb = mPresetReverb;
                if (oldPresetReverb != null) {
                    oldPresetReverb.release();
                }
            } catch (Throwable t) {
                report(t);
            }

            try {

                final PresetReverb newPresetReverb;

                if (ManufacturerUtils.isXiaomiDevice()) {

                    // Only works for Xiaomi
                    newPresetReverb = new PresetReverb(priority, audioSessionId);

                } else {

                    // Since PresetReverb is an auxiliary effect,
                    // we need to apply it to audio session 0
                    // and attach to the given media player.
                    // Otherwise, the effect will not work for some devices.
                    // See https://stackoverflow.com/a/10412949/9437681
                    newPresetReverb = new PresetReverb(priority, 0);

                    engine.attachAuxEffect(newPresetReverb.getId());
                    engine.setAuxEffectSendLevel(1.0f);

                }

                final short presetReverbIndex = getPresetReverbIndex(mPersistence.getReverb());
                newPresetReverb.setPreset(presetReverbIndex);

                newPresetReverb.setEnabled(enabled);

                mPresetReverb = newPresetReverb;
            } catch (Throwable t) {
                report(t);
            }
        }

        mLastSessionId = audioSessionId;
    }

    /**
     * This is a very important method that must be called when the player engine does not need the AudioFx anymore.
     * If this is not called then it may fail applying audio effects in {@link AudioFxApplicable#apply(MediaPlayer)} method.
     * For example, here are the steps to reproduce such a bug case:
     * 1) open the app, play some songs and apply some audio fx settings => it works well.
     * 2) close the app by pressing system back button and close the playback notification (but do not remove the app from recent).
     * 3) open the app again, play some songs and try to apply some audio fx settings => the audio fx does not work.
     * This is because we need to release all audio effects when they are not needed anymore,
     * namely, when the player engine is shutdown.
     */
    @Override
    public synchronized void release() {

        if (DEBUG) {
            Log.d(LOG_TAG, "Releasing");
        }

        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null) {
                equalizer.release();
            }

            mEqualizer = null;
        } catch (Throwable t) {
            report(t);
        }

        try {
            BassBoost bassBoost = mBassBoost;
            if (bassBoost != null) {
                bassBoost.release();
            }

            mBassBoost = null;
        } catch (Throwable t) {
            report(t);
        }

        try {
            Virtualizer virtualizer = mVirtualizer;
            if (virtualizer != null) {
                virtualizer.release();
            }

            mVirtualizer = null;
        } catch (Throwable t) {
            report(t);
        }

        try {
            PresetReverb presetReverb = mPresetReverb;
            if (presetReverb != null) {
                presetReverb.release();
            }

            mPresetReverb = null;
        } catch (Throwable t) {
            report(t);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
