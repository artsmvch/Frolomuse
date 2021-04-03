package com.frolo.muse.android

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager


fun PackageManager.canStartActivity(intent: Intent): Boolean {
    val activityInfo = intent.resolveActivityInfo(this, intent.flags)
    //return intent.resolveActivity(this) != null
    return activityInfo != null && activityInfo.exported
}

fun Context.canStartActivity(intent: Intent): Boolean {
    return packageManager.canStartActivity(intent)
}

fun Context.startActivitySafely(intent: Intent): Boolean {
    return if (canStartActivity(intent)) {
        startActivity(intent)
        true
    } else {
        false
    }
}