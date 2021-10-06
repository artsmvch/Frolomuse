package com.frolo.billing.playstore

import android.app.Activity
import android.content.Context


interface ForegroundActivityWatcher {
    val context: Context
    val foregroundActivity: Activity?
}