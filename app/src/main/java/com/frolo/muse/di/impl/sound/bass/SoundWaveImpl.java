package com.frolo.muse.di.impl.sound.bass;

import com.frolo.muse.model.sound.SoundWave;


final class SoundWaveImpl implements SoundWave {

    private final int[] levels;
    private final int maxLevel;

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