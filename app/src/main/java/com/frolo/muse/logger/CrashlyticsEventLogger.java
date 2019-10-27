package com.frolo.muse.logger;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import java.util.Map;

import io.fabric.sdk.android.Fabric;


class CrashlyticsEventLogger implements EventLogger {

    CrashlyticsEventLogger(Context context) {
        Fabric.with(context, new Crashlytics());
    }

    @Override
    public void log(String event) {
        Crashlytics.log(event);
    }

    @Override
    public void log(String event, Map<String, String> params) {
        Crashlytics.log(event);
    }

    @Override
    public void log(Throwable err) {
        Crashlytics.logException(err);
    }
}
