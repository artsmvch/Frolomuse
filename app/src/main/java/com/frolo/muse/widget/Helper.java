package com.frolo.muse.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

final class Helper {

    static final int RC_COMMAND_TOGGLE = 123;
    static final int RC_COMMAND_SKIP_TO_PREVIOUS = 124;
    static final int RC_COMMAND_SKIP_TO_NEXT = 125;
    static final int RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE = 126;
    static final int RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE = 127;
    static final int RC_OPEN_PLAYER = 128;

    private Helper() {
        throw new AssertionError("No instances!");
    }

    static PendingIntent getPendingIntent(Context context, int requestCode, Intent intent, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, requestCode, intent, flags);
        } else {
            return PendingIntent.getService(context, requestCode, intent, flags);
        }
    }

    static boolean isEnabled(Context context, Class<?> clazz) {
        int ids[] = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, clazz));
        return ids != null && ids.length > 0;
    }
}
