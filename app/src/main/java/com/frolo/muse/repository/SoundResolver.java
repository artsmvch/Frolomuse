package com.frolo.muse.repository;

import com.frolo.muse.model.sound.Sound;

import io.reactivex.Flowable;


/**
 * Resolves sounds from files.
 */
public interface SoundResolver {

    Flowable<Sound> resolve(String filepath);

}
