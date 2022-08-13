package com.frolo.audiofx2.impl;

import android.content.Context;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;

import com.frolo.audiofx2.EffectValueRange;

final class Defaults {
    final int numberOfBands;
    final int zeroBandLevel;
    final int minBandLevelRange;
    final int maxBandLevelRange;

    Defaults(Context context) {
        short numberOfBands = 5;
        short minBandLevelRange = -1500;
        short maxBandLevelRange = +1500;
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int audioSessionId = 0;
            if (audioManager != null) {
                audioSessionId = audioManager.generateAudioSessionId();
            }
            Equalizer equalizer = new Equalizer(0, audioSessionId);
            numberOfBands = equalizer.getNumberOfBands();
            minBandLevelRange = equalizer.getBandLevelRange()[0];
            maxBandLevelRange = equalizer.getBandLevelRange()[1];
        } catch (Throwable ignored) {
        }
        this.numberOfBands = numberOfBands;
        this.zeroBandLevel = 0;
        this.minBandLevelRange = minBandLevelRange;
        this.maxBandLevelRange = maxBandLevelRange;
    }

    EffectValueRange getDefaultBandFreqRange(int band) {
        switch (band) {
            case 0:
                return new EffectValueRange(30_000, 120_000);
            case 1:
                return new EffectValueRange(120_001, 460_000);
            case 2:
                return new EffectValueRange(460_001, 1_800_00);
            case 3:
                return new EffectValueRange(1_800_001, 7_000_000);
            default:
                return new EffectValueRange(7_000_000, 20_000_000);
        }
    }
}
