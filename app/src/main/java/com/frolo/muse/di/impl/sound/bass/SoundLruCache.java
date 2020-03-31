package com.frolo.muse.di.impl.sound.bass;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.frolo.muse.model.sound.Sound;


final class SoundLruCache extends LruCache<String, Sound> {

    /**
     * This is how many bytes one integer takes.
     * @return how many bytes one integer takes
     */
    static int getIntSize() {
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
