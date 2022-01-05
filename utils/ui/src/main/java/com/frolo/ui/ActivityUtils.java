package com.frolo.ui;

import android.app.Activity;

import androidx.annotation.NonNull;


public final class ActivityUtils {

    public static boolean isFinishingOrDestroyed(@NonNull Activity activity) {
        return activity.isFinishing() || activity.isDestroyed();
    }

    private ActivityUtils() {
    }
}
