package com.frolo.core.ui.carousel

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt


class CarouselBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    @ColorInt
    var surfaceColor: Int? = null
        private set

    private var surfaceColorAnimator: Animator? = null

    var onSurfaceColorChangeListener: OnSurfaceColorChangeListener? = null

    fun setSurfaceColor(@ColorInt color: Int, animated: Boolean) {
        cancelCurrentAnimator()
        if (animated) {
            surfaceColorAnimator = createColorAnimator(
                fromColor = this.surfaceColor ?: Color.TRANSPARENT,
                toColor = color
            ).apply { start() }
        } else {
            setSurfaceColorInternal(color, isIntermediate = false)
        }
    }

    private fun cancelCurrentAnimator() {
        surfaceColorAnimator?.cancel()
        surfaceColorAnimator = null
    }

    private fun endCurrentAnimator() {
        surfaceColorAnimator?.end()
        surfaceColorAnimator = null
    }

    private fun createColorAnimator(
        @ColorInt fromColor: Int,
        @ColorInt toColor: Int
    ): ValueAnimator {
        return ValueAnimator.ofArgb(fromColor, toColor).apply {
            duration = 300L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                setSurfaceColorInternal(value, isIntermediate = value == toColor)
            }
        }
    }

    private fun setSurfaceColorInternal(@ColorInt color: Int, isIntermediate: Boolean) {
        this.surfaceColor = color
        onSurfaceColorChangeListener?.onSurfaceColorChange(color, isIntermediate)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        surfaceColor?.also { safeColor ->
            canvas.drawColor(safeColor)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        endCurrentAnimator()
    }

    fun interface OnSurfaceColorChangeListener {
        fun onSurfaceColorChange(@ColorInt color: Int, isIntermediate: Boolean)
    }

}