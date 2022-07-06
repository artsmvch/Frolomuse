package com.frolo.core.ui.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.ColorInt


class RootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private val statusBarPaint: Paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.TRANSPARENT
        }
    }
    private val statusBarRect: Rect = Rect()

    @get:ColorInt
    var statusBarColor: Int
        get() = statusBarPaint.color
        set(value) {
            statusBarPaint.color = value
            invalidate()
        }

    init {
        setWillNotDraw(false)
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        statusBarRect.set(insets.systemWindowInsetLeft, 0,
            measuredWidth + insets.systemWindowInsetRight, insets.systemWindowInsetTop)
        return super.dispatchApplyWindowInsets(insets)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawRect(statusBarRect, statusBarPaint)
    }
}