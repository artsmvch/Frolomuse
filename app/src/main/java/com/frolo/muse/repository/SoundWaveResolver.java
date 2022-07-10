package com.frolo.muse.repository;

import com.frolo.muse.model.sound.SoundWave;

import io.reactivex.Flowable;


/**
 * Resolves sound waves from files.
 */
public interface SoundWaveResolver {

    Flowable<SoundWave> resolveSoundWave(String filepath);

}
