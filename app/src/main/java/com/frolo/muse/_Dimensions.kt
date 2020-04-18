package com.frolo.muse

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue


fun Float.dp2px(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.px2dp(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.dp2px(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.px2dp(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Float.sp2px(context: Context? = null): Float {
    val metrics: DisplayMetrics =
            if (context != null) context.resources.displayMetrics
            else Resources.getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, metrics)
}