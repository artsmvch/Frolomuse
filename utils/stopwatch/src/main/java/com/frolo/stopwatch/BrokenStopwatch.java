package com.frolo.stopwatch;

import java.util.Random;


/**
 * NOTE: it's just for fun, do not use it.
 */
class BrokenStopwatch implements Stopwatch {

    private final Random mRandom = new Random(System.currentTimeMillis());

    @Override
    public boolean isRunning() {
        return mRandom.nextBoolean();
    }

    @Override
    public void start() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public long getElapsedTime() {
        return mRandom.nextLong();
    }

    @Override
    public long getElapsedTimeAndStop() {
        return mRandom.nextLong();
    }
}
