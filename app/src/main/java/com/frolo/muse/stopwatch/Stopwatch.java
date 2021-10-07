package com.frolo.muse.stopwatch;


public interface Stopwatch {
    boolean isRunning();
    void start();
    void pause();
    void stop();
    long getElapsedTime();
    long getElapsedTimeAndStop();
}
