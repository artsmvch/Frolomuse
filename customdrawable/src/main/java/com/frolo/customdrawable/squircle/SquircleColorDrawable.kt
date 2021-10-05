package com.frolo.customdrawable.squircle

import android.graphics.*
import android.graphics.drawable.Drawable


/**
 * Color filled squircle drawable. It is like [android.graphics.drawable.ColorDrawable] but it is squircle.
 * [curvature] defines the curvature of the squircle shape.
 */
class SquircleColorDrawable constructor(
    private val curvature: Double = DEFAULT_SQUIRCLE_CURVATURE,
    color: Int = Color.TRANSPARENT
): Drawable() {

    private val squirclePath = Path().apply {
        fillType = Path.FillType.EVEN_ODD
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.color = color
    }

    var color: Int = color
        set(value) {
            if (field != value) {
                field = value
                paint.color = value
                invalidateSelf()
            }
        }

    init {
        validateSquircleCurvature(curvature)
        buildPathCentered(squirclePath, curvature, bounds)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        buildPathCentered(squirclePath, curvature, bounds)
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