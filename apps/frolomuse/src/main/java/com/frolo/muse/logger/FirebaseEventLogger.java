package com.frolo.muse.logger;

import android.content.Context;
import android.content.pm.InstallSourceInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Collections;
import java.util.Map;


/**
 * Implementation of {@link EventLogger} for sending events/errors to the Firebase Console.
 */
final class FirebaseEventLogger implements EventLogger {

    @NonNull
    private final Context mContext;
    @NonNull
    private final Bundle mDefaultParams;

    FirebaseEventLogger(@NonNull Context context) {
        mContext = context;
        mDefaultParams = createDefaultParams();
    }

    @NonNull
    private Bundle createDefaultParams() {
        Bundle bundle = new Bundle();
        bundle.putString("device_info", Const.DEVICE_INFO);
        try {
            // Checking the install source info
            String packageName = mContext.getPackageName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                InstallSourceInfo sourceInfo = mContext.getPackageManager().getInstallSourceInfo(packageName);
                bundle.putString("install_source_initiating", sourceInfo.getInitiatingPackageName());
                bundle.putString("install_source_originating", sourceInfo.getOriginatingPackageName());
                bundle.putString("install_source_installing", sourceInfo.getInstallingPackageName());
            } else {
                String installerPackageName = mContext.getPackageManager().getInstallerPackageName(packageName);
                bundle.putString("installer_package", installerPackageName);
            }
        } catch (Throwable ignored) {
        }
        return bundle;
    }

    @Override
    public void log(String event) {
        log(event, Collections.emptyMap());
    }

    @Override
    public void log(String event, Map<String, String> params) {
        final int paramsMapSize = params != null ? params.size() : 0;
        final Bundle bundle = new Bundle(mDefaultParams.size() + paramsMapSize);
        bundle.putAll(mDefaultParams);
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
