package com.frolo.muse.di.impl.local;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.sound.Sound;
import com.frolo.muse.repository.SoundResolver;

import java.util.Random;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;


public class CheapSoundResolverImpl implements SoundResolver {

    private static class SoundImpl implements Sound {
        final int[] frameGains;
        final int maxFrameGain;

        SoundImpl(int[] frameGains, int maxFrameGain) {
            this.frameGains = frameGains;
            this.maxFrameGain = maxFrameGain;
        }

        @Override
        public int getFrameCount() {
            return frameGains != null ? frameGains.length : 0;
        }

        @Override
        public int getFrameGainAt(int position) {
            return frameGains[position];
        }

        @Override
        public int getMaxFrameGain() {
            return maxFrameGain;
        }
    }

    @Override
    public Flowable<Sound> resolve(String filepath) {
        return Flowable.fromCallable(new Callable<Sound>() {
            @Override
            public Sound call() throws Exception {
                final Random random = new Random(System.currentTimeMillis());
                final int frameCount = BuildConfig.SOUND_FRAME_GAIN_COUNT;
                final int[] frameGains = new int[frameCount];
                int maxFrameGain = Integer.MIN_VALUE;
                for (int i = 0; i < frameCount; i++) {
                    int v = 50 + random.nextInt(125);
                    frameGains[i] = v;
                    if (v > maxFrameGain) {
                        maxFrameGain = v;
                    }
                }
                try {
                    Thread.sleep(250 + random.nextInt(1250));
                } catch (InterruptedException ignored) {
                }
                return new SoundImpl(frameGains, maxFrameGain);
            }
        });
    }

}
