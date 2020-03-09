package com.frolo.muse.di.impl.sound.android;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;


final class SoundFileLruCache extends LruCache<String, SoundFile> {

    SoundFileLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull SoundFile value) {
        return value.getLevelsLength() * getIntSize();
    }

    /**
     * This is how much memory one int variable takes.
     * @return size of one int variable
     */
    private int getIntSize() {
        return 4;
    }

}