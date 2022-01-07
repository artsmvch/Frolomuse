package com.frolo.muse.views.checkable

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import kotlin.math.max


class SqrCheckMarkDrawable constructor(
    private val backgroundColor: Int,
    private val checkMarkColor: Int
) : Drawable(), Animatable {

    private val checkMarkPath = Path()
    private var checkMarkAlpha = 255

    // paint tools
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // animation
    private var animator: ValueAnimator? = null
    private val animListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(anim: Animator) {
            invalidateSelf()
        }

        override fun onAnimationEnd(anim: Animator) {
            invalidateSelf()
        }

        override fun onAnimationCancel(anim: Animator) {
            invalidateSelf()
        }

        override fun onAnimationStart(anim: Animator) {
            invalidateSelf()
        }
    }

    // resolve path according to current bounds
    private fun resolvePath() {
        val rect = bounds
        val halfOfWidth = rect.width() / 2f
        val halfOfHeight = rect.height() / 2f
        val cx = rect.left + halfOfWidth
        val cy = rect.top + halfOfHeight
        val rx = halfOfWidth
        val ry = halfOfHeight

        checkMarkPath.reset()

        checkMarkPath.moveTo(cx - rx / 2f, cy)

        checkMarkPath.lineTo(cx - rx / 6, cy + ry / 2.8f)

        checkMarkPath.lineTo(cx + rx / 1.8f, cy - ry / 2.8f)

        checkMarkPath.fillType = Path.FillType.EVEN_ODD
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        resolvePath()
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        resolvePath()
    }

    override fun draw(canvas: Canvas) {
        val rect = bounds
        val left = rect.left.toFloat()
        val top = rect.top.toFloat()
        val right = rect.right.toFloat()
        val bottom = rect.bottom.toFloat()

        paint.apply {
            color = backgroundColor
            style = Paint.Style.FILL
            alpha = 255
        }
        canvas.drawRect(left, top, right, bottom, paint)

        paint.apply {
            color = checkMarkColor
            style = Paint.Style.STROKE
            alpha = checkMarkAlpha
            strokeWidth = max(rect.width(), rect.height()) / 20f
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(checkMarkPath, paint)
    }

    override fun setAlpha(alpa: Int) {
    }

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun isRunning(): Boolean {
        return animator.let { it != null && it.isRunning }
    }

    override fun start() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(1, 255).apply {
            addUpdateListener { anim ->
                checkMarkAlpha = anim.animatedValue as Int
                invalidateSelf()
            }
            addListener(animListener)
            interpolator = LinearInterpolator()
            duration = DURATION_CHECK_MARK
            startDelay = DELAY_CHECK_MARK
            start()
        }
    }

    override fun stop() {
        animator?.cancel()
    }
}