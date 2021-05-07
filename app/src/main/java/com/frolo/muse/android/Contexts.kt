package com.frolo.muse.android

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.hardware.display.DisplayManagerCompat


val Context.windowManager: WindowManager?
    get() {
        return getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

val Context.clipboardManager: ClipboardManager?
    get() {
        return getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

val Context.notificationManager: NotificationManager?
    get() {
        return getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

val Context.notificationManagerCompat: NotificationManagerCompat
    get() {
        return NotificationManagerCompat.from(this)
    }

val Context.displayCompat: Display?
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display ?: windowManager?.defaultDisplay
        } else {
            windowManager?.defaultDisplay
        }
    }

val Context.displayManager: DisplayManager?
    get() {
        return getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
    }

val Context.displayManagerCompat: DisplayManagerCompat?
    get() {
        return DisplayManagerCompat.getInstance(this)
    }

val Context.activityManager: ActivityManager?
    get() {
        return getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    }