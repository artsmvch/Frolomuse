package com.frolo.muse.di.impl.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.frolo.core.ui.ApplicationWatcher;
import com.frolo.muse.permission.PermissionChecker;
import com.frolo.ui.ActivityUtils;


public final class PermissionCheckerImpl implements PermissionChecker {

    private static final String P_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    private final Context mContext;

    public PermissionCheckerImpl(@NonNull Context context) {
        mContext = context;
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
        Activity foreground = ApplicationWatcher.getForegroundActivity();
        if (foreground != null && !ActivityUtils.isFinishingOrDestroyed(foreground)) {
            // Not correct solution below?
            //return !foreground.shouldShowRequestPermissionRationale(P_READ_EXTERNAL_STORAGE);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
}
