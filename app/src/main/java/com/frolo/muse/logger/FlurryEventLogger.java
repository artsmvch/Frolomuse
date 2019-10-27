package com.frolo.muse.logger;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.frolo.muse.BuildConfig;

import java.util.Map;

class FlurryEventLogger implements EventLogger {

    FlurryEventLogger(Context context) {
        // initializing Flurry
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(context, BuildConfig.FLURRY_KEY);
    }

    @Override
    public void log(String event) {
        FlurryAgent.logEvent(event);
    }

    @Override
    public void log(String event, Map<String, String> params) {
        FlurryAgent.logEvent(event, params);
    }

    @Override
    public void log(Throwable err) {
        FlurryAgent.logEvent(err.getClass() + ": " + err.getMessage());
    }
}
