package com.frolo.muse.di.impl.sound.android;

import androidx.collection.LruCache;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.sound.Sound;
import com.frolo.muse.repository.SoundResolver;

import java.io.File;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;


public class AndroidSoundResolverImpl implements SoundResolver {

    private static class SoundImpl implements Sound {

        final SoundFile soundFile;

        SoundImpl(SoundFile soundFile) {
            this.soundFile = soundFile;
        }

        @Override
        public int getFrameCount() {
            return soundFile.getLevelsLength();
        }

        @Override
        public int getFrameGainAt(int position) {
            return (int) soundFile.getLevels()[position];
        }

        @Override
        public int getMaxFrameGain() {
            return (int) soundFile.getMaxLevel();
        }
    }

    private static int calculateCacheSize() {
        return 1 * 1024 * 1024; // 1 mb
    }

    private final LruCache<String, SoundFile> cache =
            new SoundFileLruCache(calculateCacheSize());

    @Override
    public Flowable<Sound> resolve(final String filepath) {
        return Flowable.fromCallable(new Callable<Sound>() {
            @Override
            public Sound call() throws Exception {
                SoundFile soundFile = cache.get(filepath);
                if (soundFile == null) {
                    soundFile = SoundFile.create(new File(filepath), BuildConfig.SOUND_FRAME_GAIN_COUNT);
                    if (soundFile == null) {
                        throw new NullPointerException("Failed to create SoundFile for filepath " + filepath);
                    }
                    cache.put(filepath, soundFile);
                }
                return new SoundImpl(soundFile);
            }
        });
    }
}