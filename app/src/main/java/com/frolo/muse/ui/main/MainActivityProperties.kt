package com.frolo.muse.ui.main

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import com.frolo.debug.DebugUtils
import com.frolo.muse.R
import com.frolo.ui.StyleUtils


internal class MainActivityProperties(
    private val activity: Activity
) {

    private val context: Context get() = activity
    private val resources: Resources get() = context.resources

    @get:ColorInt
    val colorPrimary: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPrimary)
    }

    @get:ColorInt
    val colorPrimaryDark: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPrimaryDark)
    }

    @get:ColorInt
    val colorPrimarySurface: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorPrimarySurface)
    }

    @get:ColorInt
    val colorSurface: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorSurface)
    }

    @get:ColorInt
    val actionModeBackgroundColor: Int by lazy {
        try {
            StyleUtils.resolveColor(context, R.attr.actionModeBackground)
        } catch (error: Throwable) {
            // This is probably a drawable
            DebugUtils.dumpOnMainThread(error)
            StyleUtils.resolveColor(context, android.R.attr.navigationBarColor)
        }
    }

    @get:Px
    val playerSheetPeekHeight: Int by lazy {
        resources.getDimension(R.dimen.player_sheet_peek_height).toInt()
    }

    @get:Px
    val playerSheetCornerRadius: Int by lazy {
        resources.getDimension(R.dimen.player_sheet_corner_radius).toInt()
    }

    @get:Dimension
    val bottomNavigationCornerRadius: Float by lazy {
        resources.getDimension(R.dimen.bottom_navigation_bar_corner_radius)
    }

    val fragmentContentInsets: Rect by lazy {
        val left = resources.getDimension(R.dimen.fragment_content_left_inset).toInt()
        val top = resources.getDimension(R.dimen.fragment_content_top_inset).toInt()
        val right = resources.getDimension(R.dimen.fragment_content_right_inset).toInt()
        val bottom = resources.getDimension(R.dimen.fragment_content_bottom_inset).toInt()
        Rect(left, top, right, bottom)
    }
}