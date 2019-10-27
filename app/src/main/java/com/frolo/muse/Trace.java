package com.frolo.muse;

import android.util.Log;

import com.crashlytics.android.Crashlytics;


public final class Trace {
    private static final String TAG = "MusicPlayer_";
    private static final boolean ENABLED = BuildConfig.DEBUG;

    private Trace() {
        throw new AssertionError("No instances!");
    }

    private static String getTag() {
        return TAG;
    }

    private static String getTag(String key) {
        return TAG + key;
    }

    public static void e(Throwable throwable) {
        Crashlytics.logException(throwable);
        if (ENABLED) {
            Log.e(getTag(), throwable.getMessage(), throwable);
        }
    }

    public static void e(String key, Throwable throwable) {
        Crashlytics.logException(throwable);
        if (ENABLED) {
            Log.e(getTag(key), throwable.getMessage(), throwable);
        }
    }

    public static void e(String key, String msg) {
        if (ENABLED) {
            Log.e(getTag(key), msg);
        }
    }

    public static void e(String key, String msg, Throwable throwable) {
        if (ENABLED) {
            Log.e(getTag(key), msg, throwable);
            e(throwable);
        }
    }

    public static void d(String key, String message) {
        if (ENABLED) {
            Log.d(getTag(key), message);
        }
    }

    public static void i(String key, String message) {
        if (ENABLED) {
            Log.i(getTag(key), message);
        }
    }

    public static void w(String key, String message) {
        if (ENABLED) {
            Log.w(getTag(key), message);
        }
    }

    public static void report(String message) {
        Crashlytics.log(message);
    }
}
