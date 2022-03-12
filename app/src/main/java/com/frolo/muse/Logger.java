package com.frolo.muse;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;


/**
 * Logger for the app.
 * There are methods for info, debug and error logging.
 * Errors are also reported to the remote analytics service.
 * In the release build, the logging (except remote analytics) is disabled.
 */
public final class Logger {
    private static final String TAG_PREFIX = "Frolomuse_";
    private static final boolean ENABLED = BuildConfig.DEBUG;

    private Logger() {
        throw new AssertionError("No instances!");
    }

    private static String buildDefaultTag() {
        return TAG_PREFIX;
    }

    private static String buildTag(String key) {
        return TAG_PREFIX + key;
    }

    /**
     * Reports the <code>t</code> error to the remote service.
     * Currently, we're using Firebase as the remote analytics service.
     * Every error (fatal or not) will be available in the Firebase Console.
     * @param t error
     */
    private static void report(Throwable t) {
        if (BuildConfig.GOOGLE_SERVICES_ENABLED) {
            FirebaseCrashlytics.getInstance().recordException(t);
        }
    }

    public static void e(Throwable throwable) {
        report(throwable);
        if (ENABLED) {
            Log.e(buildDefaultTag(), throwable.getMessage(), throwable);
        }
    }

    public static void e(String key, Throwable throwable) {
        report(throwable);
        if (ENABLED) {
            Log.e(buildTag(key), throwable.getMessage(), throwable);
        }
    }

    public static void e(String key, String msg) {
        if (ENABLED) {
            Log.e(buildTag(key), msg);
        }
    }

    public static void e(String key, String msg, Throwable throwable) {
        if (ENABLED) {
            Log.e(buildTag(key), msg, throwable);
            e(throwable);
        }
    }

    public static void d(String key, String message) {
        if (ENABLED) {
            Log.d(buildTag(key), message);
        }
    }

    public static void i(String key, String message) {
        if (ENABLED) {
            Log.i(buildTag(key), message);
        }
    }

    public static void w(String key, String message) {
        if (ENABLED) {
            Log.w(buildTag(key), message);
        }
    }

}
