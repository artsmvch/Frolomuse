package com.frolo.muse.engine;

import com.frolo.muse.model.preset.Preset;
import com.frolo.muse.model.reverb.Reverb;


/**
 * Simple implementation of {@link AudioFxObserver} that does nothing in callback methods.
 */
public abstract class SimpleAudioFxObserver implements AudioFxObserver {
    @Override
    public void onEnabled(AudioFx audioFx) {
    }

    @Override
    public void onDisabled(AudioFx audioFx) {
    }

    @Override
    public void onBandLevelChanged(AudioFx audioFx, short band, short level) {
    }

    @Override
    public void onPresetUsed(AudioFx audioFx, Preset preset) {
    }

    @Override
    public void onBassStrengthChanged(AudioFx audioFx, short strength) {
    }

    @Override
    public void onVirtualizerStrengthChanged(AudioFx audioFx, short strength) {
    }

    @Override
    public void onReverbUsed(AudioFx audioFx, Reverb reverb) {
    }
}
