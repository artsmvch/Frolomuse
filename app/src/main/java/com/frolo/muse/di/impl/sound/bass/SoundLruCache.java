package com.frolo.muse.di.impl.sound.bass;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.frolo.muse.model.sound.Sound;


final class SoundLruCache extends LruCache<String, Sound> {

    /**
     * This is how many bytes one level takes. Since a level is
     * represented by an integer, and in JVM, it is 4 bytes.
     * @return how many bytes one level takes
     */
    static int getLevelSize() {
        return 4;
    }

    SoundLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Sound value) {
        return value.getFrameCount() * getLevelSize();
    }

}
