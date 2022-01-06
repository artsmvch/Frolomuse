package com.frolo.audiofx;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Helper class to persist the state of AudioFx.
 * The state is stored in a SharedPreferences whose name is specified by the client.
 * To create an instance, call {@link AudioFxPersistence#create(Context, String)} method.
 */
final class AudioFxPersistence {

    static final int FLAG_EQ_USE_NO_PRESET = 0;
    static final int FLAG_EQ_USE_NATIVE_PRESET = 1;
    static final int FLAG_EQ_USE_CUSTOM_PRESET = 2;

    static AudioFxPersistence create(@NonNull Context context, @NonNull String prefsName) {
        return new AudioFxPersistence(context, prefsName);
    }

    private static final String KEY_PREFIX = "audiofx.";
    
    private static final String KEY_ENABLED = KEY_PREFIX + "enabled";

    private static final String KEY_EQ_USE_FLAG = KEY_PREFIX + "eq_use_flag";
    private static final String KEY_EQ_BAND_LEVELS = KEY_PREFIX + "eq_band_levels";
    private static final String KEY_EQ_LAST_NATIVE_PRESET = KEY_PREFIX + "eq_last_native_preset";
    private static final String KEY_EQ_LAST_CUSTOM_PRESET = KEY_PREFIX + "eq_last_custom_preset";

    private static final String KEY_BASS_STRENGTH = KEY_PREFIX + "bass_strength";
    private static final String KEY_VIRTUALIZER_STRENGTH = KEY_PREFIX + "virtualizer_strength";

    private static final String KEY_REVERB = KEY_PREFIX + "reverb";

    private final Context mContext;
    private final String mPrefsName;

    private final SharedPreferences mPrefs;

    private volatile boolean mStateRestored = false;

    // The internal state
    private volatile boolean mEnabled;

    private volatile int mEqUseFlag = FLAG_EQ_USE_NO_PRESET;
    private volatile NativePreset mLastNativePreset;
    private volatile CustomPreset mLastCustomPreset;
    private volatile short[] mBandLevels;

    private volatile short mBassStrength = 0;

    private volatile short mVirtualizerStrength = 0;

    private volatile Reverb mReverb = Reverb.NONE;

    private AudioFxPersistence(@NonNull Context context, @NonNull String prefsName) {
        mContext = context;
        mPrefsName = prefsName;
        mPrefs = mContext.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE);
    }

    private synchronized void checkStateRestored() {
        if (!mStateRestored) {

            mEnabled = mPrefs.getBoolean(KEY_ENABLED, false);

            mEqUseFlag = mPrefs.getInt(KEY_EQ_USE_FLAG, FLAG_EQ_USE_NO_PRESET);
            mLastNativePreset =
                Serialization.tryDeserializeNativePreset(mPrefs.getString(KEY_EQ_LAST_NATIVE_PRESET, null));
            mLastCustomPreset =
                Serialization.tryDeserializeCustomPreset(mPrefs.getString(KEY_EQ_LAST_CUSTOM_PRESET, null));
            mBandLevels = Serialization.tryDeserializeShorts(mPrefs.getString(KEY_EQ_BAND_LEVELS, null));

            mBassStrength = (short) mPrefs.getInt(KEY_BASS_STRENGTH, 0);

            mVirtualizerStrength = (short) mPrefs.getInt(KEY_VIRTUALIZER_STRENGTH, 0);

            Reverb deserializedReverb =
                    Serialization.tryDeserializeReverb(mPrefs.getInt(KEY_REVERB, 0));
            mReverb = deserializedReverb != null ? deserializedReverb : Reverb.NONE;

            mStateRestored = true;
        }
    }

    synchronized boolean isEnabled() {
        checkStateRestored();
        return mEnabled;
    }

    synchronized void saveEnabled(boolean enabled) {
        checkStateRestored();
        mEnabled = enabled;
        mPrefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    synchronized int getEqUseFlag() {
        checkStateRestored();
        return mEqUseFlag;
    }

    synchronized void saveEqUseFlag(int flag) {
        checkStateRestored();
        mEqUseFlag = flag;
        mPrefs.edit().putInt(KEY_EQ_USE_FLAG, flag).apply();
    }

    synchronized short[] getLastBandLevels() {
        checkStateRestored();
        if (mBandLevels == null) mBandLevels = new short[5];
        return mBandLevels;
    }

    synchronized void saveBandLevel(short band, short level) {
        checkStateRestored();

        if (mBandLevels == null) mBandLevels = new short[5];
        // TODO: expand the array if needed
        if (band >= 0 && band < mBandLevels.length) {
            mBandLevels[band] = level;
        }

        // TODO: persist changes in the preferences
    }

    @Nullable
    synchronized NativePreset getLastNativePreset() {
        checkStateRestored();
        return mLastNativePreset;
    }

    synchronized void saveLastNativePreset(NativePreset preset) {
        checkStateRestored();
        mLastNativePreset = preset;

        // TODO: persist changes in the preferences
    }

    @Nullable
    synchronized CustomPreset getLastCustomPreset() {
        checkStateRestored();
        return mLastCustomPreset;
    }

    synchronized void saveLastCustomPreset(CustomPreset preset) {
        checkStateRestored();
        mLastCustomPreset = preset;

        // TODO: persist changes in the preferences
    }

    synchronized short getBassStrength() {
        checkStateRestored();
        return mBassStrength;
    }

    synchronized void saveBassStrength(short strength) {
        checkStateRestored();
        mBassStrength = strength;
        mPrefs.edit().putInt(KEY_BASS_STRENGTH, strength).apply();
    }

    synchronized short getVirtualizerStrength() {
        checkStateRestored();
        return mVirtualizerStrength;
    }

    synchronized void saveVirtualizerStrength(short strength) {
        checkStateRestored();
        mVirtualizerStrength = strength;
        mPrefs.edit().putInt(KEY_VIRTUALIZER_STRENGTH, strength).apply();
    }

    synchronized Reverb getReverb() {
        checkStateRestored();
        // Reverb cannot be null
        if (mReverb == null) mReverb = Reverb.NONE;
        return mReverb;
    }

    synchronized void saveReverb(Reverb reverb) {
        checkStateRestored();
        mReverb = reverb;
        mPrefs.edit().putInt(KEY_REVERB, Serialization.trySerializeReverb(reverb)).apply();
    }

    synchronized void save() {
        final SharedPreferences.Editor editor = mPrefs.edit();

        editor.putBoolean(KEY_ENABLED, mEnabled);

        editor.putInt(KEY_EQ_USE_FLAG, mEqUseFlag);
        editor.putString(KEY_EQ_BAND_LEVELS, Serialization.trySerializeShorts(mBandLevels));
        editor.putString(KEY_EQ_LAST_NATIVE_PRESET, Serialization.trySerializeNativePreset(mLastNativePreset));
        editor.putString(KEY_EQ_LAST_CUSTOM_PRESET, Serialization.trySerializeCustomPreset(mLastCustomPreset));

        editor.putInt(KEY_BASS_STRENGTH, mBassStrength);

        editor.putInt(KEY_VIRTUALIZER_STRENGTH, mVirtualizerStrength);

        editor.putInt(KEY_REVERB, Serialization.trySerializeReverb(mReverb));

        editor.apply();
    }

    @Override
    protected void finalize() throws Throwable {
        save();
        super.finalize();
    }
}
