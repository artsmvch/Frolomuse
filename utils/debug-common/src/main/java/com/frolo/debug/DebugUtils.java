package com.frolo.debug;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;


public final class DebugUtils {

    private static class MainThreadHolder {
        private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    }

    private static final AtomicBoolean ENABLED_REF = new AtomicBoolean(false);

    public static boolean isDebug() {
        return ENABLED_REF.get();
    }

    public static void setDebug(boolean enabled) {
        ENABLED_REF.set(enabled);
    }

    private static void postOnMainThread(@NonNull Runnable action) {
        MainThreadHolder.HANDLER.post(action);
    }

    public static void dump(@NonNull Throwable error) {
        if (ENABLED_REF.get()) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else {
                throw new RuntimeException(error);
            }
        }
    }

    public static void dumpOnMainThread(@NonNull Throwable error) {
        if (ENABLED_REF.get()) {
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
