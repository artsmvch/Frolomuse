package com.frolo.muse.ui.main.player.waveform;

import androidx.annotation.NonNull;

import com.frolo.muse.model.sound.Sound;
import com.frolo.muse.views.sound.WaveformSeekBar;


/**
 * A Waveform implementation based on {@link Sound}.
 * All methods delegate the call to the Sound object.
 */
public class SoundWaveform implements WaveformSeekBar.Waveform {

    private Sound sound;

    public SoundWaveform(@NonNull Sound sound) {
        this.sound = sound;
    }

    @Override
    public int getWaveCount() {
        return sound.getFrameCount();
    }

    @Override
    public int getWaveAt(int index) {
        return sound.getFrameGainAt(index);
    }

    @Override
    public int getMaxWave() {
        return sound.getMaxFrameGain();
    }
}
