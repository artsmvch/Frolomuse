package com.frolo.audiofx;


public interface AudioFxObserver {

    void onEnabled(AudioFx audioFx);

    void onDisabled(AudioFx audioFx);

    void onBandLevelChanged(AudioFx audioFx, short band, short level);

    void onPresetUsed(AudioFx audioFx, Preset preset);

    void onBassStrengthChanged(AudioFx audioFx, short strength);

    void onVirtualizerStrengthChanged(AudioFx audioFx, short strength);

    void onReverbUsed(AudioFx audioFx, Reverb reverb);

}