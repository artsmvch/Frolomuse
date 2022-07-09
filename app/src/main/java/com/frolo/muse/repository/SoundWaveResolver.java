package com.frolo.muse.repository;

import com.frolo.muse.model.sound.SoundWave;

import io.reactivex.Flowable;


/**
 * Resolves sound waves from files.
 */
public interface SoundWaveResolver {

    int DEFAULT_LEVEL_COUNT = 65;

    Flowable<SoundWave> resolveSoundWave(String filepath);

}
