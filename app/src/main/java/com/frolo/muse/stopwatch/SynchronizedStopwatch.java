package com.frolo.muse.stopwatch;


class SynchronizedStopwatch implements Stopwatch {

    private final Object mLock = new Object();
    private final Stopwatch mStopwatch;

    SynchronizedStopwatch(Stopwatch stopwatch) {
        mStopwatch = stopwatch;
    }

    @Override
    public boolean isRunning() {
        synchronized (mLock) {
            return mStopwatch.isRunning();
        }
    }

    @Override
    public void start() {
        synchronized (mLock) {
            mStopwatch.start();
        }
    }

    @Override
    public void pause() {
        synchronized (mLock) {
            mStopwatch.pause();
        }
    }

    @Override
    public void stop() {
        synchronized (mLock) {
            mStopwatch.stop();
        }
    }

    @Override
    public long getElapsedTime() {
        synchronized (mLock) {
            return mStopwatch.getElapsedTime();
        }
    }

    @Override
    public long getElapsedTimeAndStop() {
        synchronized (mLock) {
            return mStopwatch.getElapsedTimeAndStop();
        }
    }
}
