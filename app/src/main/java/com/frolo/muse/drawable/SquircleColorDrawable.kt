package com.frolo.muse.drawable

import android.graphics.*

import android.graphics.drawable.Drawable

import androidx.annotation.ColorInt
import com.frolo.muse.math.buildFullSquirclePath
import com.frolo.muse.math.validateSquircleCurvature
import kotlin.math.min

/**
 * Color filled squircle drawable. It is like [android.graphics.drawable.ColorDrawable] but it is squircle.
 * [curvature] defines the curvature of the squircle shape.
 */
class SquircleColorDrawable constructor(
    private val curvature: Double,
    @ColorInt color: Int = Color.TRANSPARENT
): Drawable() {

    private val matrix = Matrix()

    private val squirclePath = Path().apply {
        fillType = Path.FillType.EVEN_ODD
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.style = Paint.Style.FILL
        this.color = color
    }

    @ColorInt
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
        buildPathCentered(bounds)
    }

    private fun buildPathCentered(bounds: Rect?) {
        if (bounds == null || bounds.isEmpty) {
            squirclePath.reset()
            return
        }

        val width = bounds.width()
        val height = bounds.height()

        val radius = min(width, height) / 2
        buildFullSquirclePath(squirclePath, radius, curvature)

        matrix.setTranslate(width / 2f - radius, height / 2f - radius)
        squirclePath.transform(matrix)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        buildPathCentered(bounds)
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