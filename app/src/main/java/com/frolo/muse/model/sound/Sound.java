package com.frolo.muse.model.sound;


/**
 * Represents the sound of an audio file.
 * Contains some useful method to access the sound info.
 */
public interface Sound {

    int getFrameCount();

    int getFrameGainAt(int position);

    int getMaxFrameGain();

}
