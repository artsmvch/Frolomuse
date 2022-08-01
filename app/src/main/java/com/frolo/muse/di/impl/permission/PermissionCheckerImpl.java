package com.frolo.muse.di.impl.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.frolo.ui.ActivityUtils;
import com.frolo.core.ui.ActivityWatcher;
import com.frolo.muse.permission.PermissionChecker;


public final class PermissionCheckerImpl implements PermissionChecker {

    private static final String P_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    private final Context mContext;
    private final ActivityWatcher mActivityWatcher;

    public PermissionCheckerImpl(@NonNull Context context, @NonNull ActivityWatcher activityWatcher) {
        mContext = context;
        mActivityWatcher = activityWatcher;
    }

    @Override
    public boolean isQueryMediaContentPermissionGranted() {
        final int result = ContextCompat.checkSelfPermission(mContext, P_READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requireQueryMediaContentPermission() throws SecurityException {
        if (!isQueryMediaContentPermissionGranted()) {
            throw new SecurityException();
        }
    }

    @Override
    public boolean shouldRequestMediaPermissionInSettings() {
        Activity foreground = mActivityWatcher.getForegroundActivity();
        if (foreground != null && !ActivityUtils.isFinishingOrDestroyed(foreground)) {
            // Not correct solution below?
            //return !foreground.shouldShowRequestPermissionRationale(P_READ_EXTERNAL_STORAGE);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
}
