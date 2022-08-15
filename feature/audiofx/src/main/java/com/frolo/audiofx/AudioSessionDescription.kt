package com.frolo.audiofx

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

class AudioSessionDescription(
    private val context: Context,
    private val applicationInfo: ApplicationInfo,
    val audioSessionId: Int
) {
    val name: CharSequence? by lazy {
        applicationInfo.runCatching { loadLabel(context.packageManager) }.getOrNull()
    }
    val icon: Drawable? by lazy {
        applicationInfo.runCatching { loadIcon(context.packageManager) }.getOrNull()
    }
}