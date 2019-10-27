package com.frolo.muse.ui

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics


fun Float.toPx(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.toDp(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.toPx(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.toDp(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}