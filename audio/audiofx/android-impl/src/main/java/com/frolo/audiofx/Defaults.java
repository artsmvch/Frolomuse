package com.frolo.audiofx;

import android.media.audiofx.Equalizer;

final class Defaults {
    static final short DEFAULT_NUMBER_OF_BANDS;
    static final short DEFAULT_BAND_LEVEL;
    static final short DEFAULT_MIN_BAND_LEVEL_RANGE;
    static final short DEFAULT_MAX_BAND_LEVEL_RANGE;

    static {
        short numberOfBands = 5;
        short minBandLevelRange = -15000;
        short maxBandLevelRange = +15000;
        try {
            Equalizer equalizer = new Equalizer(0, 0);
            numberOfBands = equalizer.getNumberOfBands();
            minBandLevelRange = equalizer.getBandLevelRange()[0];
            maxBandLevelRange = equalizer.getBandLevelRange()[1];
        } catch (Throwable ignored) {
        }
        DEFAULT_NUMBER_OF_BANDS = numberOfBands;
        DEFAULT_BAND_LEVEL = 0;
        DEFAULT_MIN_BAND_LEVEL_RANGE = minBandLevelRange;
        DEFAULT_MAX_BAND_LEVEL_RANGE =  maxBandLevelRange;
    }

    static int[] getDefaultBandFreqRange(short band) {
        switch (band) {
            case 0:
                return new int[] { 30_000, 120_000 };
            case 1:
                return new int[] { 120_001, 460_000 };
            case 2:
                return new int[] { 460_001, 1_800_00 };
            case 3:
                return new int[] { 1_800_001, 7_000_000 };
            default:
                return new int[] { 7_000_000, 20_000_000 };
        }
    }
}
