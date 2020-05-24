package com.frolo.muse.logger;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Map;


class AndroidEventLogger implements EventLogger {

    private String buildTag() {
        return "FrolomuseEvent";
    }

    @Nullable
    private String getParamsString(@Nullable final Map<String, String> params) {
        if (params == null || params.size() == 0)
            return null;

        final StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');
        }

        if (sb.length() >= 1) {
            // Removing the last '&'
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public void log(String event) {
        Log.d(buildTag(), event);
    }

    @Override
    public void log(String event, Map<String, String> params) {
        final String paramsString = getParamsString(params);
        if (paramsString != null && !paramsString.isEmpty()) {
            Log.d(buildTag(), event + " [" + paramsString + "]");
        } else {
            Log.d(buildTag(), event);
        }
    }

    @Override
    public void log(Throwable err) {
        Log.e(buildTag(), "Error", err);
    }

}
