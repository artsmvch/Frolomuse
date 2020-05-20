package com.frolo.muse.logger;

import android.util.Log;

import java.util.Map;

class AndroidEventLogger implements EventLogger {
    private String buildTag() {
        return "Frolomuse_";
    }

    @Override
    public void log(String event) {
        Log.d(buildTag(), event);
    }

    @Override
    public void log(String event, Map<String, String> params) {
        // TODO: params are not used, need to fix that
        Log.d(buildTag(), event);
    }

    @Override
    public void log(Throwable err) {
        Log.e(buildTag(), "", err);
    }
}
