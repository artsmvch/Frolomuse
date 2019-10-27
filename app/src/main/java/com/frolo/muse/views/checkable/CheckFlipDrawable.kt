package com.frolo.muse.views.checkable

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.annotation.UiThread


@UiThread
class CheckFlipDrawable constructor(
        private var target: Drawable? = null,
        private val checkMark: Drawable
) : Drawable() {

    // state of flipper
    private var checked: Boolean = false

    private var flipAlpha = 255

    // recalculate the middle of bounds every time the bounds change
    private var middleX: Int = 0

    // animating
    private var flipProgress: Float = if (checked) 1f else 0f
    private var animator: ValueAnimator? = null

    // callbacks for both first and second
    private val callback = object : Callback {
        override fun invalidateDrawable(d: Drawable) {
            //invalidateSelf()
        }
        override fun scheduleDrawable(d: Drawable, r: Runnable, t: Long) = Unit
        override fun unscheduleDrawable(d: Drawable, r: Runnable) = Unit
    }

    init {
        checkMark.callback = callback
    }

    override fun draw(canvas: Canvas) {
        if (flipProgress < 0.5f) {
            // drawing target (if target is not null)
            target?.let { drawable ->
                val progressHalfOfWidth = (bounds.width() * (0.5f - flipProgress)).toInt()
                drawable.setBounds(middleX - progressHalfOfWidth, bounds.top, middleX + progressHalfOfWidth, bounds.bottom)
                drawable.draw(canvas)
            }
        } else {
            // drawing check mark
            checkMark.let { drawable ->
                val progressHalfOfWidth = (bounds.width() * (flipProgress - 0.5f)).toInt()
                drawable.setBounds(middleX - progressHalfOfWidth, bounds.top, middleX + progressHalfOfWidth, bounds.bottom)
                drawable.draw(canvas)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        flipAlpha = alpha
        target?.alpha = alpha
        checkMark.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        middleX = bounds.left + (bounds.right - bounds.left) / 2
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        middleX = left + (right - left) / 2
    }

    private fun invalidateFlipProgress() {
        flipProgress = if (checked) 1f else 0f
        invalidateSelf()
    }

    private fun startAnimatingFlip() {
        val startValue = animator.let { anim ->
            val value = if (anim != null && anim.isRunning) {
                anim.animatedValue as Float
            } else {
                if (checked) 0f else 1f
            }

            anim?.cancel()

            value
        }

        val targetValue = if (checked) 1f else 0f

        var askedToAnimateDrawable = false

        animator = ValueAnimator.ofFloat(startValue, targetValue).apply {
            addUpdateListener { anim ->
                flipProgress = anim.animatedValue as Float
                if (checked && flipProgress > 0.5f && !askedToAnimateDrawable) {
                    askedToAnimateDrawable = true
                    (checkMark as? Animatable)?.start()
                } else if (!checked && flipProgress < 0.5f && !askedToAnimateDrawable) {
                    askedToAnimateDrawable = true
                    (target as? Animatable)?.start()
                }
                invalidateSelf()
            }
            interpolator = LinearInterpolator()
            duration = DURATION_CHECK_FLIP
            start()
        }
    }

    fun setTarget(d: Drawable?) {
        target = d?.also { drawable ->
            drawable.callback = callback
            drawable.alpha = flipAlpha
        }
        invalidateSelf()
    }

    fun setChecked(checked: Boolean, animate: Boolean) {
        this.checked = checked
        if (animate) {
            startAnimatingFlip()
        } else {
            invalidateFlipProgress()
        }
    }

}