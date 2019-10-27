package com.frolo.muse;

import android.os.Looper;

public final class ThreadStrictMode {

    // Check only in debug mode
    private static final boolean ENABLED = BuildConfig.DEBUG;

    private ThreadStrictMode() { }

    private static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static boolean isOnBackgroundThread() {
        return !isOnMainThread();
    }

    public static void assertMain() {
        if (ENABLED && !isOnMainThread()) {
            throw new IllegalStateException("Called on a background thread");
        }
    }

    public static void assertBackground() {
        if (ENABLED && !isOnBackgroundThread()) {
            throw new IllegalStateException("Called on the main thread");
        }
    }
}
