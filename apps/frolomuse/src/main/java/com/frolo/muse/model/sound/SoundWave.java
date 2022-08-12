package com.frolo.muse.model.sound;


/**
 * Represents sound waveform of an audio file.
 */
public interface SoundWave {
    int length();

    int getLevelAt(int position);

    int getMaxLevel();
}
