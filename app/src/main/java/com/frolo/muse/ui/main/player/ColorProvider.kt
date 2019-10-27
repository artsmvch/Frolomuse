package com.frolo.muse.ui.main.player

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import com.frolo.muse.R


class ColorProvider(context: Context) {
    @ColorInt
    val colorModeOn: Int

    @ColorInt
    val colorModeOff: Int

    init {
        @ColorInt val colors = IntArray(2)
        val attrs = intArrayOf(R.attr.modeOnTint, R.attr.modeOffTint)
        val ta = context.obtainStyledAttributes(attrs)
        colors[0] = ta.getColor(0, Color.RED)
        colors[1] = ta.getColor(1, Color.LTGRAY)
        ta.recycle()

        colorModeOn = colors[0]
        colorModeOff = colors[1]
    }
}