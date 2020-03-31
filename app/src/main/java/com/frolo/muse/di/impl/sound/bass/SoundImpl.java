package com.frolo.muse.di.impl.sound.bass;

import com.frolo.muse.model.sound.Sound;


class SoundImpl implements Sound {

    private final int[] frameGains;
    private final int maxFrameGain;

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