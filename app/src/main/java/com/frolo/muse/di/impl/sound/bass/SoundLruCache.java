package com.frolo.muse.di.impl.sound.bass;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.frolo.muse.model.sound.Sound;


final class SoundLruCache extends LruCache<String, Sound> {

    /**
     * Returns the size of one variable of type int in bytes.
     * @return the size of one variable of type int in bytes
     */
    private static int getIntSize() {
        return 4;
    }

    SoundLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Sound value) {
        return value.getFrameCount() * getIntSize();
    }

}
