package com.frolo.muse.di.impl.sound.android;

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

    @Override
    public Flowable<Sound> resolve(final String filepath) {
        return Flowable.fromCallable(new Callable<Sound>() {
            @Override
            public Sound call() throws Exception {
                SoundFile soundFile =  SoundFile.create(new File(filepath), BuildConfig.SOUND_FRAME_GAIN_COUNT);
                return new SoundImpl(soundFile);
            }
        });
    }

}
