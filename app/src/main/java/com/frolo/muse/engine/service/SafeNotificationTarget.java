package com.frolo.muse.engine.service;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.frolo.muse.Logger;

// This class provides a factory-method for creating a safe notification target.
// Its purpose is to avoid crashes on API v21-22. See https://fabric.io/frolovs-projects/android/apps/com.frolo.musp/issues/5bdb530ff8b88c2963298876.
public final class SafeNotificationTarget {
    private SafeNotificationTarget() { }

    public static Target<Bitmap> create(Context context,
                                    int viewId, RemoteViews remoteViews, Notification notification, int notificationId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new NotificationTarget(context, viewId, remoteViews, notification, notificationId);
        } else {
            return new NotificationTargetPreV22Only(context, viewId, remoteViews, notification, notificationId);
        }
    }

    private static class NotificationTargetPreV22Only extends NotificationTarget {
        private final static String TAG = NotificationTargetPreV22Only.class.getSimpleName();

        NotificationTargetPreV22Only(Context context, int viewId, RemoteViews remoteViews, Notification notification, int notificationId) {
            super(context, viewId, remoteViews, notification, notificationId);
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            try {
                // Wrapping super into try-catch statement as it throws and exception on API v21-22 sometimes.
                // See https://fabric.io/frolovs-projects/android/apps/com.frolo.musp/issues/5bdb530ff8b88c2963298876
                super.onResourceReady(resource, transition);
            } catch (Throwable e) {
                Logger.e(TAG, e);
            }
        }
    }
}
