package com.frolo.muse.di.impl.sound.bass;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.frolo.muse.model.sound.SoundWave;


final class SoundWaveLruCache extends LruCache<String, SoundWave> {

    /**
     * This is how many bytes one level takes. Since a level is
     * represented by an integer, and in JVM, it is 4 bytes.
     * @return how many bytes one level takes
     */
    static int getLevelSize() {
        return 4;
    }

    SoundWaveLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull SoundWave value) {
        return value.length() * getLevelSize();
    }

}
