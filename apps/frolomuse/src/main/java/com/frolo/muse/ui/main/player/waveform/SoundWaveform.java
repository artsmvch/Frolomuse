package com.frolo.muse.ui.main.player.waveform;

import androidx.annotation.NonNull;

import com.frolo.muse.model.sound.SoundWave;
import com.frolo.waveformseekbar.WaveformSeekBar;

/**
 * A Waveform implementation based on {@link SoundWave}.
 * All methods delegate the call to the SoundWave object.
 */
public class SoundWaveform implements WaveformSeekBar.Waveform {

    private final SoundWave soundWave;

    public SoundWaveform(@NonNull SoundWave soundWave) {
        this.soundWave = soundWave;
    }

    @Override
    public int getWaveCount() {
        return soundWave.length();
    }

    @Override
    public int getWaveAt(int index) {
        return soundWave.getLevelAt(index);
    }

    @Override
    public int getMaxWave() {
        return soundWave.getMaxLevel();
    }
}
