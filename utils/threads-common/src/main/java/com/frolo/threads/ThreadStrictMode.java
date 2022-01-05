package com.frolo.threads;

import com.frolo.debug.DebugUtils;


public final class ThreadStrictMode {

    public static void assertMain() {
        if (DebugUtils.isDebug() && !ThreadUtils.isOnMainThread()) {
            throw new IllegalStateException("Called on a background thread");
        }
    }

    public static void assertBackground() {
        if (DebugUtils.isDebug() && !ThreadUtils.isOnBackgroundThread()) {
            throw new IllegalStateException("Called on the main thread");
        }
    }

    private ThreadStrictMode() {
    }
}
