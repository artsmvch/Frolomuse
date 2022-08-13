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
import androidx.annotation.Nullable;

import com.frolo.audiofx.applicable.AudioFxApplicable;
import com.frolo.vendor.ManufacturerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.frolo.audiofx.android.BuildConfig;


/**
 * Implementation of {@link AudioFxApplicable} based on the AudioFx effects from the Android SDK.
 */
public final class AudioFxImpl implements AudioFxApplicable {

    private static final String LOG_TAG = "AudioFxImpl";

    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * If this flag is set to true, then, when {@link AudioFxApplicable#applyTo} is called,
     * the AudioFx can omit the initialization of all audio effects if the audio session does NOT change.
     *
     * If this flag is set to false, then the AudioFx initialize all audio effects
     * every time {@link AudioFxApplicable#applyTo} is called.
     *
     * Be careful by settings this to false, because it may be not an optimal solution.
     */
    private static final boolean OPTIMIZE_AUDIO_SESSION_CHANGE = true;

    public interface ErrorHandler {
        void onError(Throwable error);
    }

    @NonNull
    public static AudioFxImpl getInstance(@NonNull Context context, @NonNull String prefsName) {
        return getInstance(context, prefsName, null);
    }

    @NonNull
    public static AudioFxImpl getInstance(
            @NonNull Context context, @NonNull String prefsName, @Nullable ErrorHandler errorHandler) {
        return new AudioFxImpl(context, prefsName, errorHandler);
    }

    private static short clamp(short value, short minValue, short maxValue) {
        if (value < minValue) return minValue;
        if (value > maxValue) return maxValue;
        return value;
    }

    private final Context mContext;

    /**
     * Remember the last session ID so we can compare it later when {@link AudioFxApplicable#applyTo(MediaPlayer)} method gets called.
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

    private final Defaults mDefaults;

    AudioFxImpl(Context context, String prefsName, ErrorHandler errorHandler) {
        mDefaults = new Defaults(context);
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
            Log.d(LOG_TAG, "Init: " + describeState());
        }
    }

    private void report(Throwable t) {
        AudioFxException error = new AudioFxException(t);
        if (DEBUG) Log.e(LOG_TAG, "A critical error occurred", error);
        if (mErrorHandler != null) {
            mErrorHandler.onError(error);
        }
    }

    private synchronized String describeState() {
        return '{' +
                "equalizer=" + describeEffectState(mEqualizer) +
                ", " +
                "reverb=" + describeEffectState(mPresetReverb) +
                ", " +
                "bass=" + describeEffectState(mBassBoost) +
                ", " +
                "virtualizer=" + describeEffectState(mVirtualizer) +
                "}";
    }

    private String describeEffectState(@Nullable AudioEffect effect) {
        if (effect == null) {
            return "null";
        }
        return "{id=" + effect.getId() + ", " +
                "{name=" + effect.getDescriptor().name + ", " +
                "enabled=" + effect.getEnabled() + '}';
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
    public synchronized void setEnabled(boolean enabled) {
        mPersistence.saveEnabled(enabled);

        trySetEnabled(mEqualizer, enabled);
        trySetEnabled(mBassBoost, enabled);
        trySetEnabled(mVirtualizer, enabled);
        trySetEnabled(mPresetReverb, enabled);

        if (enabled) {
            mObserverRegistry.dispatchEnabled();
        } else {
            mObserverRegistry.dispatchDisabled();
        }
    }

    private void trySetEnabled(@Nullable AudioEffect effect, boolean enabled) {
        try {
            if (effect != null) {
                effect.setEnabled(enabled);
            }
        } catch (Throwable t) {
            report(t);
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
        } catch (Throwable t) {
            report(t);
        }
        return mDefaults.minBandLevelRange;
    }

    @Override
    public short getMaxBandLevelRange() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null) {
                return equalizer.getBandLevelRange()[1];
            }
        } catch (Throwable t) {
            report(t);
        }
        return mDefaults.maxBandLevelRange;
    }

    @Override
    public int[] getBandFreqRange(short band) {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null) {
                return equalizer.getBandFreqRange(band);
            }
        } catch (Throwable t) {
            report(t);
        }
        return mDefaults.getDefaultBandFreqRange(band);
    }

    @Override
    public short getNumberOfBands() {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null) {
                return equalizer.getNumberOfBands();
            }
        } catch (Throwable t) {
            report(t);
        }
        return mDefaults.numberOfBands;
    }

    @Override
    public short getBandLevel(short band) {
        try {
            Equalizer equalizer = mEqualizer;
            if (equalizer != null) {
                return equalizer.getBandLevel(band);
            }
            short[] levels = mPersistence.getLastBandLevels();
            if (levels != null && band < levels.length) {
                return levels[band];
            }
        } catch (Throwable t) {
            report(t);
        }
        return mDefaults.zeroBandLevel;
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
                    CustomPreset customPreset = (CustomPreset) preset;
                    int numberOfBands = equalizer.getNumberOfBands();
                    for (short band = 0; band < Math.min(numberOfBands, customPreset.getLevelCount()); band++) {
                        short level = customPreset.getLevelAt(band);
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
        strength = clamp(strength, getMinBassStrength(), getMaxBassStrength());
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
        strength = clamp(strength, getMinVirtualizerStrength(), getMaxVirtualizerStrength());
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
    public synchronized void applyTo(@NonNull MediaPlayer engine) {
        applyToImpl(engine.getAudioSessionId(), engine);
    }

    @Override
    public synchronized void applyTo(int audioSessionId) {
        applyToImpl(audioSessionId, null);
    }

    private synchronized void applyToImpl(int audioSessionId, @Nullable MediaPlayer engine) {
        final Integer currSessionId = mLastSessionId;

        final boolean sessionHasChanged = currSessionId == null || currSessionId != audioSessionId;

        // We can omit the initialization if and only if the optimization is enabled and the session has not changed
        final boolean canOmitInitialization = OPTIMIZE_AUDIO_SESSION_CHANGE && !sessionHasChanged;

        final int priority = 0;

        final boolean enabled = mPersistence.isEnabled();

        if (DEBUG) {
            String msg = "Pre-apply to MediaPlayer: " +
                    "session_id=" + audioSessionId + ", " +
                    "session_has_changed=" + sessionHasChanged + ", " +
                    "can_omit_initialization=" + canOmitInitialization + ", " +
                    "priority=" + priority + ", " +
                    "enabled=" + enabled + ", " +
                    "state=" + describeState();
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
                    if (preset != null && preset.getLevelCount() > 0) {
                        for (int i = 0; i < preset.getLevelCount(); i++) {
                            newEqualizer.setBandLevel((short) i, preset.getLevelAt(i));
                        }
                        adjustBandLevels = false;
                    } else {
                        adjustBandLevels = true;
                    }
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
                } else if (engine != null) {
                    // Since PresetReverb is an auxiliary effect,
                    // we need to apply it to audio session 0
                    // and attach to the given media player.
                    // Otherwise, the effect will not work for some devices.
                    // See https://stackoverflow.com/a/10412949/9437681
                    newPresetReverb = new PresetReverb(priority, 0);
                    engine.attachAuxEffect(newPresetReverb.getId());
                    engine.setAuxEffectSendLevel(1.0f);
                } else {
                    newPresetReverb = null;
                }

                if (newPresetReverb != null) {
                    short presetReverbIndex = getPresetReverbIndex(mPersistence.getReverb());
                    newPresetReverb.setPreset(presetReverbIndex);
                    newPresetReverb.setEnabled(enabled);
                }

                mPresetReverb = newPresetReverb;
            } catch (Throwable t) {
                report(t);
            }
        }

        mLastSessionId = audioSessionId;

        if (DEBUG) {
            String msg = "Post-apply to MediaPlayer: " +
                    "state=" + describeState();
            Log.d(LOG_TAG, msg);
        }
    }

    /**
     * This is a very important method that must be called when the player engine does not need the AudioFx anymore.
     * Not calling this method may affect the application of sound effects later.
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
            Log.d(LOG_TAG, "Releasing: " + describeState());
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
