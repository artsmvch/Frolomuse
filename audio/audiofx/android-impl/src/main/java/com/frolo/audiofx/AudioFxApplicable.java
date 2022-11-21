package com.frolo.audiofx;

import android.media.MediaPlayer;

import androidx.annotation.NonNull;

/**
 * An extension for {@link AudioFx} that is able to apply its audio effects to a particular instance of {@link MediaPlayer},
 * and then release all the applied audio effects.
 * {@link AudioFxApplicable#release()} should be called at the end of work with an instance of AudioFx.
 * NOTE: Android specific.
 */
@Deprecated
public interface AudioFxApplicable extends AudioFx {

    /**
     * Applies all audio effects to the given audio session ID.
     */
    void applyTo(int audioSessionId);

    /**
     * Applies all audio effects to the given MediaPlayer.
     */
    void applyTo(@NonNull MediaPlayer engine);

    /**
     * Release all the applied audio effects.
     */
    void release();

}