package com.frolo.customdrawable.squircle

import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.roundToInt


/**
 * Squircle-stroked drawable. It draws a squircle shape of the specified color.
 * [curvature] defines the curvature of the squircle shape.
 */
class SquircleStrokeDrawable(
    private val curvature: Double = DEFAULT_SQUIRCLE_CURVATURE,
    strokeWidth: Float = 0f,
    strokeColor: Int = Color.TRANSPARENT
) : Drawable() {

    private val squirclePath = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var strokeWidth: Float = strokeWidth
        set(value) {
            if (field != value) {
                field = value
                paint.strokeWidth = value
                buildPath(bounds, value)
                invalidateSelf()
            }
        }

    var strokeColor: Int = strokeColor
        set(value) {
            if (field != value) {
                field = value
                paint.color = value
                invalidateSelf()
            }
        }

    init {
        validateSquircleCurvature(curvature)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        buildPath(bounds, strokeWidth)
    }

    override fun onBoundsChange(bounds: Rect) {
        buildPath(bounds, strokeWidth)
    }

    private fun buildPath(bounds: Rect?, strokeWidth: Float) {
        // We use this offset to squeeze the path a little towards the center of the shape
        val innerOffset = (strokeWidth / 2f).roundToInt()
        val targetBounds: Rect? = if (bounds != null
                && bounds.width() > 2 * innerOffset
                && bounds.height() > 2 * innerOffset) {
            // To draw the line inside the shape and not along the outline of the shape
            Rect(bounds.left + innerOffset, bounds.top + innerOffset,
                    bounds.right - innerOffset, bounds.bottom - innerOffset)
        } else {
            bounds
        }
        buildPathCentered(squirclePath, curvature, targetBounds)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(squirclePath, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

}