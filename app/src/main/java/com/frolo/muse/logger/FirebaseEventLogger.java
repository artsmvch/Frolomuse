package com.frolo.muse.logger;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Collections;
import java.util.Map;


/**
 * Implementation of {@link EventLogger} for sending events/errors to the Firebase Console.
 */
final class FirebaseEventLogger implements EventLogger {

    /**
     * Returns info about this device.
     * E.g. Samsung Note 4 6.0 MARSHMALLOW, LGE LG-D410 4.4.2 KITKAT.
     * @return info about this device
     */
    private static String getDeviceInfo() {
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();
    }

    private final Context mContext;

    FirebaseEventLogger(Context context) {
        mContext = context;
    }

    @Override
    public void log(String event) {
        log(event, Collections.emptyMap());
    }

    @Override
    public void log(String event, Map<String, String> params) {
        final int paramsMapSize = params != null ? params.size() : 0;
        final Bundle bundle = new Bundle(paramsMapSize + 1); // +1 for device info param
        bundle.putString("device_info", getDeviceInfo());
        if (params != null) {
            for (final Map.Entry<String, String> entry : params.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
        }
        FirebaseAnalytics.getInstance(mContext).logEvent(event, bundle);
    }

    @Override
    public void log(Throwable err) {
        if (err != null) {
            FirebaseCrashlytics.getInstance().recordException(err);
        }
    }

}
