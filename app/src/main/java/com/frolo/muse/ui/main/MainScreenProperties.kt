package com.frolo.muse.ui.main

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.frolo.core.graphics.Palette
import com.frolo.debug.DebugUtils
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import kotlin.concurrent.getOrSet


@UiThread
internal class MainScreenProperties(
    private val activity: Activity
) {

    private val context: Context get() = activity
    private val resources: Resources get() = context.resources

    private val hsl: ThreadLocal<FloatArray> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ThreadLocal.withInitial { FloatArray(3) }
        } else {
            ThreadLocal<FloatArray>()
        }
    }

    private fun getHslTmp(): FloatArray {
        return hsl.getOrSet { FloatArray(3) }
    }

    val isLightTheme: Boolean by lazy {
        StyleUtils.resolveBool(context, R.attr.isLightTheme)
    }

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

    @get:ColorInt
    val playerStatusBarBackground: Int by lazy {
        Color.TRANSPARENT //ContextCompat.getColor(context, R.color.player_status_bar_background)
    }

    @get:ColorInt
    val playerToolbarElementBackground: Int by lazy {
        ContextCompat.getColor(context, R.color.player_toolbar_element_background)
    }

    @get:ColorInt
    val colorModeOff: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.iconImageTint)
    }

    @get:ColorInt
    val colorModeOn: Int by lazy {
        StyleUtils.resolveColor(context, R.attr.colorAccent)
    }

    @get:Px
    val playerSheetPeekHeight: Int by lazy {
        resources.getDimension(R.dimen.player_sheet_peek_height).toInt()
    }

    @get:Px
    val playerSheetCornerRadius: Int by lazy {
        resources.getDimension(R.dimen.player_sheet_corner_radius).toInt()
    }

    @get:ColorInt
    val defaultArtBackgroundColor: Int by lazy {
        validateArtBackgroundColorLightness(colorPrimary)
    }

    @get:Dimension
    val bottomNavigationCornerRadius: Float by lazy {
        resources.getDimension(R.dimen.bottom_navigation_bar_corner_radius)
    }

    @get:ColorInt
    val transparentStatusBarColor: Int = Color.TRANSPARENT

    val fragmentContentInsets: Rect by lazy {
        val left = resources.getDimension(R.dimen.fragment_content_left_inset).toInt()
        val top = resources.getDimension(R.dimen.fragment_content_top_inset).toInt()
        val right = resources.getDimension(R.dimen.fragment_content_right_inset).toInt()
        val bottom = resources.getDimension(R.dimen.fragment_content_bottom_inset).toInt()
        Rect(left, top, right, bottom)
    }

    @ColorInt
    fun getModeColor(on: Boolean): Int {
        return if (on) colorModeOn else colorModeOff
    }

    @ColorInt
    fun extractArtBackgroundColor(palette: Palette?): Int {
        @ColorInt
        val colorFromPalette: Int? = when {
            palette == null -> null
            isLightTheme -> palette.getSwatch(Palette.Target.DARK_MUTED)?.rgb
            else -> palette.getSwatch(Palette.Target.LIGHT_MUTED)?.rgb
        }
        return colorFromPalette?.let(::validateArtBackgroundColorLightness)
            ?: defaultArtBackgroundColor
    }

    @ColorInt
    private fun validateArtBackgroundColorLightness(@ColorInt color: Int): Int {
        val hsl = getHslTmp()
        ColorUtils.colorToHSL(color, hsl)
        val originalLightness = hsl[2]
        val targetLightness = if(isLightTheme) {
            originalLightness.coerceIn(0.05f, 0.6f)
        } else {
            originalLightness.coerceIn(0.4f, 0.85f)
        }
        hsl[2] = targetLightness
        return ColorUtils.HSLToColor(hsl)
    }
}