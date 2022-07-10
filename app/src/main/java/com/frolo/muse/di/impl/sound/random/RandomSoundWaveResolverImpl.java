package com.frolo.muse.di.impl.sound.random;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.sound.SoundWave;
import com.frolo.muse.repository.SoundWaveResolver;
import com.frolo.threads.ThreadStrictMode;

import java.util.Random;

import io.reactivex.Flowable;


public final class RandomSoundWaveResolverImpl implements SoundWaveResolver {

    private static class SoundWaveImpl implements SoundWave {
        final int[] levels;
        final int maxLevel;

        SoundWaveImpl(int[] levels, int maxLevel) {
            this.levels = levels;
            this.maxLevel = maxLevel;
        }

        @Override
        public int length() {
            return levels != null ? levels.length : 0;
        }

        @Override
        public int getLevelAt(int position) {
            return levels[position];
        }

        @Override
        public int getMaxLevel() {
            return maxLevel;
        }
    }

    @Override
    public Flowable<SoundWave> resolveSoundWave(String filepath) {
        return Flowable.fromCallable(() -> {
            ThreadStrictMode.assertBackground();
            final Random random = new Random(System.currentTimeMillis());
            final int levelCount = BuildConfig.SOUND_WAVEFORM_LENGTH;
            final int[] levels = new int[levelCount];
            int maxLevel = Integer.MIN_VALUE;
            for (int i = 0; i < levelCount; i++) {
                int v = 50 + random.nextInt(125);
                levels[i] = v;
                if (v > maxLevel) {
                    maxLevel = v;
                }
            }
            try {
                Thread.sleep(250 + random.nextInt(1250));
            } catch (InterruptedException ignored) {
            }
            return new SoundWaveImpl(levels, maxLevel);
        });
    }

}
