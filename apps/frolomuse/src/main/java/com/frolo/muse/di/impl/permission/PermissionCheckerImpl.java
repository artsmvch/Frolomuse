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
    public static final String READ_AUDIO_PERMISSION;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            READ_AUDIO_PERMISSION = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            READ_AUDIO_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }
    public static final String WRITE_AUDIO_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private final Context mContext;

    public PermissionCheckerImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public boolean isReadAudioPermissionGranted() {
        final int result = ContextCompat.checkSelfPermission(mContext, READ_AUDIO_PERMISSION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean shouldRequestReadAudioPermissionInSettings() {
        Activity foreground = ApplicationWatcher.getForegroundActivity();
        if (foreground != null && !ActivityUtils.isFinishingOrDestroyed(foreground)) {
            // Not correct solution below?
            //return !foreground.shouldShowRequestPermissionRationale(P_READ_EXTERNAL_STORAGE);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
}
