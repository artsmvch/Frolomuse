package com.frolo.stopwatch;

import androidx.annotation.NonNull;


public final class Stopwatches {

    @NonNull
    public static Stopwatch createSimple() {
        return new SimpleStopwatch();
    }

    @NonNull
    public static Stopwatch makeSynchronized(@NonNull Stopwatch stopwatch) {
        return new SynchronizedStopwatch(stopwatch);
    }

    @NonNull
    public static Stopwatch createSimpleSynchronized() {
        SimpleStopwatch stopwatch = new SimpleStopwatch();
        return new SynchronizedStopwatch(stopwatch);
    }

    @NonNull
    public static Stopwatch createBroken() {
        return new BrokenStopwatch();
    }

    private Stopwatches() {
    }
}
