package com.frolo.muse;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;


public final class DebugUtils {

    private static final boolean ENABLED = BuildConfig.DEBUG;

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private static void runOnMainThread(@NonNull Runnable action) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            action.run();
        } else {
            sMainHandler.post(action);
        }
    }

    private static void postOnMainThread(@NonNull Runnable action) {
        sMainHandler.post(action);
    }

    public static void dump(@NonNull Throwable error) {
        if (ENABLED) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else {
                throw new RuntimeException(error);
            }
        }
    }

    public static void dumpOnMainThread(@NonNull Throwable error) {
        if (ENABLED) {
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    dump(error);
                }
            };
            postOnMainThread(action);
        }
    }

    private DebugUtils() {
    }
}
