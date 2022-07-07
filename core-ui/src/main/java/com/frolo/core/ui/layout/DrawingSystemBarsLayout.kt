package com.frolo.core.ui.layout

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class DrawingSystemBarsLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private var lastInsets: WindowInsetsCompat? = null
    private var statusBarDrawable: Drawable? = null

    init {
        setWillNotDraw(false)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (lastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.requestApplyInsets(this)
        }
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        lastInsets = WindowInsetsCompat.toWindowInsetsCompat(insets, this)
        return super.dispatchApplyWindowInsets(insets)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        statusBarDrawable?.also { drawable ->
            val statusBarTopInset = lastInsets?.systemWindowInsetTop ?: 0
            drawable.setBounds(0, 0, measuredWidth, statusBarTopInset)
            drawable.draw(canvas)
        }
    }

    fun setStatusBarDrawable(drawable: Drawable?) {
        this.statusBarDrawable = drawable
        invalidate()
    }

    fun setStatusBarColor(@ColorInt color: Int) {
        setStatusBarDrawable(ColorDrawable(color))
    }
}