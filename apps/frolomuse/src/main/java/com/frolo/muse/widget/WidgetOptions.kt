package com.frolo.muse.widget

import android.appwidget.AppWidgetManager
import android.os.Bundle


data class WidgetOptions(
    val minWidth: Int,
    val minHeight: Int,
    val maxWidth: Int,
    val maxHeight: Int
) {
    val allPropertiesAreValid: Boolean get() {
        return minWidth != 0 && minHeight != 0 && maxWidth != 0 && maxHeight != 0
    }
}

fun WidgetOptions.apply(newWidgetOptions: WidgetOptions): WidgetOptions {
    val newMinWidth = if (newWidgetOptions.minWidth != 0) newWidgetOptions.minWidth else minWidth
    val newMinHeight = if (newWidgetOptions.minHeight != 0) newWidgetOptions.minHeight else minHeight
    val newMaxWidth = if (newWidgetOptions.maxWidth != 0) newWidgetOptions.maxWidth else maxWidth
    val newMaxHeight = if (newWidgetOptions.maxHeight != 0) newWidgetOptions.maxHeight else maxHeight
    return WidgetOptions(
        minWidth = newMinWidth,
        minHeight = newMinHeight,
        maxWidth = newMaxWidth,
        maxHeight = newMaxHeight
    )
}

fun Bundle.toWidgetOptions(): WidgetOptions {
    val minWidth = this.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val minHeight = this.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    val maxWidth = this.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
    val maxHeight = this.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
    return WidgetOptions(
        minWidth = minWidth,
        minHeight = minHeight,
        maxWidth = maxWidth,
        maxHeight = maxHeight
    )
}

fun AppWidgetManager.getWidgetOptions(appWidgetId: Int, newWidgetOptionsBundle: Bundle? = null): WidgetOptions? {
    val newWidgetOptions = newWidgetOptionsBundle?.toWidgetOptions()
    if (newWidgetOptions != null && newWidgetOptions.allPropertiesAreValid) {
        return newWidgetOptions
    }
    val currWidgetOptions = this.getAppWidgetOptions(appWidgetId)?.toWidgetOptions()
    if (newWidgetOptions == null) {
        return currWidgetOptions
    }
    if (currWidgetOptions != null) {
        return currWidgetOptions.apply(newWidgetOptions)
    }
    return newWidgetOptions
}