package com.frolo.muse;

import android.app.Activity;

import org.jetbrains.annotations.NotNull;


public final class ActivityUtils {

    public static boolean isFinishingOrDestroyed(@NotNull Activity activity) {
        return activity.isFinishing() || activity.isDestroyed();
    }

    private ActivityUtils() {
    }
}
