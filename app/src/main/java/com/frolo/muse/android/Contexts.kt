package com.frolo.muse.android

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.Display
import android.view.WindowManager


val Context.windowManager: WindowManager?
    get() {
        return getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

val Context.clipboardManager: ClipboardManager?
    get() {
        return getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

val Context.displayCompat: Display?
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display ?: windowManager?.defaultDisplay
        } else {
            windowManager?.defaultDisplay
        }
    }