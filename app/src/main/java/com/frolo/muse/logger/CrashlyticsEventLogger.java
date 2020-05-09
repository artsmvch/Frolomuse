package com.frolo.muse.logger;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Map;


class CrashlyticsEventLogger implements EventLogger {

    CrashlyticsEventLogger(/*unused*/ Context context) {
    }

    @Override
    public void log(String event) {
        FirebaseCrashlytics.getInstance().log(event);
    }

    @Override
    public void log(String event, /*unused*/ Map<String, String> params) {
        // TODO: compose the full message using the params
        FirebaseCrashlytics.getInstance().log(event);
    }

    @Override
    public void log(Throwable err) {
        FirebaseCrashlytics.getInstance().recordException(err);
    }
}
