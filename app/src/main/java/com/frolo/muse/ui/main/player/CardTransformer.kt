package com.frolo.muse.ui.main.player

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class CardTransformer constructor(
        private val minScale: Float = 0.8f,
        private val maxScale: Float = 1.0f
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val safePosition = when {
            position < -1f -> -1f
            position > 1f -> 1f
            else -> position
        }

        val tempScale = if (safePosition < 0) (1 + safePosition) else (1 - safePosition)

        val slope = (maxScale - minScale) / 1
        val scaleValue = minScale + tempScale * slope
        page.scaleX = scaleValue
        page.scaleY = scaleValue
    }
}
