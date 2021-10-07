package com.frolo.muse.stopwatch;


class SimpleStopwatch implements Stopwatch {

    private static final long NO_TIME = -1L;

    static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private boolean mStarted = false;
    private long mAccumulatedTimeSum = 0L;
    private long mLastAccumulationTime = NO_TIME;

    @Override
    public boolean isRunning() {
        return mStarted;
    }

    @Override
    public void start() {
        if (mStarted) {
            return;
        }

        mStarted = true;
        mLastAccumulationTime = currentTimeMillis();
    }

    @Override
    public void pause() {
        if (!mStarted) {
            return;
        }

        accumulateImpl();

        mStarted = false;
    }

    @Override
    public void stop() {
        // The result is ignored
        getElapsedTimeAndStop();
    }

    @Override
    public long getElapsedTime() {
        return accumulateImpl();
    }

    @Override
    public long getElapsedTimeAndStop() {
        long elapsedTime = accumulateImpl();

        // Clearing state
        mStarted = false;
        mAccumulatedTimeSum = 0L;
        mLastAccumulationTime = NO_TIME;

        return elapsedTime;
    }

    private long accumulateImpl() {
        if (mStarted) {
            long currentTime = currentTimeMillis();
            long elapsedSinceLastAccumulation = currentTime - mLastAccumulationTime;
            mAccumulatedTimeSum += elapsedSinceLastAccumulation;
            mLastAccumulationTime = currentTime;
        }

        return mAccumulatedTimeSum;
    }
}
