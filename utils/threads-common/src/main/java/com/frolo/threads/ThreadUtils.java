package com.frolo.threads;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;


public final class ThreadUtils {

    private static class MainThreadHolder {
        static final Handler HANDLER = new Handler(Looper.getMainLooper());
    }

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static boolean isOnBackgroundThread() {
        return !isOnMainThread();
    }

    public static void runOnMainThread(@NonNull Runnable action) {
        if (isOnMainThread()) {
            action.run();
        } else {
            MainThreadHolder.HANDLER.post(action);
        }
    }

    public static void postOnMainThread(@NonNull Runnable action) {
        MainThreadHolder.HANDLER.post(action);
    }

    public static void postOnMainThread(long delayMillis, @NonNull Runnable action) {
        MainThreadHolder.HANDLER.postDelayed(action, delayMillis);
    }

    private ThreadUtils() {
    }
}
