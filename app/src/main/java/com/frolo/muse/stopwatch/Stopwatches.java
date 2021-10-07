package com.frolo.muse.stopwatch;

import org.jetbrains.annotations.NotNull;


public final class Stopwatches {

    @NotNull
    public static Stopwatch createSimple() {
        return new SimpleStopwatch();
    }

    @NotNull
    public static Stopwatch makeSynchronized(@NotNull Stopwatch stopwatch) {
        return new SynchronizedStopwatch(stopwatch);
    }

    @NotNull
    public static Stopwatch createSimpleSynchronized() {
        SimpleStopwatch stopwatch = new SimpleStopwatch();
        return new SynchronizedStopwatch(stopwatch);
    }

    @NotNull
    public static Stopwatch createBroken() {
        return new BrokenStopwatch();
    }

    private Stopwatches() {
    }
}
