package com.frolo.muse.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

import com.frolo.muse.R


@Deprecated(
    message = "The shape of this widget does not look smooth",
    replaceWith = ReplaceWith("ShapeableImageView")
)
class RoundedCornerImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val path = Path()
    private val rect = RectF()
    private val cornerRadius: Float

    init {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornerImageView)
        cornerRadius = arr.getDimension(R.styleable.RoundedCornerImageView_imageCornerRadius, 0f)
        arr.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }

}