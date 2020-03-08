package com.frolo.muse.ui.main.player.waveform;

import com.frolo.muse.views.sound.WaveformSeekBar;


/**
 * A Waveform implementation based on a static wave value;
 */
public class StaticWaveform implements WaveformSeekBar.Waveform {

    private final int count;
    private final int value;
    private final int maxValue;

    public StaticWaveform(int count, int value, int maxValue) {
        this.count = count;
        this.value = value;
        this.maxValue = maxValue;
    }

    @Override
    public int getWaveCount() {
        return count;
    }

    @Override
    public int getWaveAt(int index) {
        return value;
    }

    @Override
    public int getMaxWave() {
        return maxValue;
    }
}
