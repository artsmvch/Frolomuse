package com.frolo.muse.views.spring

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView


/**
 * Applies spring animation to overscrolling and overflinging.
 * NOTE: both overscroll and overfling work perfect on the stock version of Android SDK.
 * I.e. it will work as expected on such vendors like Google Pixel, Google Nexus and so on.
 * But sometimes overflinging may not work on Xiaomi.
 * It's because of strange implementation of the OverScroller that may give curr velocity as 0.
 */
class SpringRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val springManager = SpringEdgeEffect.Manager(this)

    private var shouldTranslateSelf = true

    private var isTopFadingEdgeEnabled = true

    init {
        edgeEffectFactory = springManager.createFactory()
    }

    override fun draw(canvas: Canvas) {
        springManager.withSpring(canvas, shouldTranslateSelf) {
            super.draw(canvas)
            false
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        springManager.withSpring(canvas, !shouldTranslateSelf) {
            super.dispatchDraw(canvas)
            false
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (isTopFadingEdgeEnabled) super.getTopFadingEdgeStrength() else 0f
    }
}
