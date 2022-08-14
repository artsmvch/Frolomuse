package com.frolo.audiofx.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.frolo.audiofx.ui.R
import com.frolo.ui.Screen
import kotlin.math.min


class EffectBoosterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.effectBoosterViewStyle
): View(context, attrs, defStyleAttr) {

    @FloatRange(from = 0.0, to = 1.0)
    var boostValue: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var strokeWidth: Float = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    @ColorInt
    var strokeActiveColor: Int = Color.TRANSPARENT
        private set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    @ColorInt
    var strokeInactiveColor: Int = Color.TRANSPARENT
        private set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var onBoostValueChangeListener: OnBoostValueChangeListener? = null

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ovalRect: RectF = RectF()

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.EffectBoosterView,
            defStyleAttr, R.style.EffectBoosterView_Default)
        try {
            strokeWidth = a.getDimension(R.styleable.EffectBoosterView_strokeWidth,
                Screen.dpFloat(context, 4f))
            strokeInactiveColor = a.getColor(R.styleable.EffectBoosterView_strokeInactiveColor,
                Color.TRANSPARENT)
            strokeActiveColor = a.getColor(R.styleable.EffectBoosterView_strokeActiveColor,
                Color.TRANSPARENT)
        } finally {
            a.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val contentWidth = measuredWidth - paddingLeft - paddingRight
        val contentHeight = measuredHeight - paddingTop - paddingBottom
        val radius = min(contentWidth, contentHeight) / 2f - strokeWidth / 2f
        val centerX = paddingLeft + contentWidth / 2f
        val centerY = measuredHeight - paddingBottom
        ovalRect.set(centerX - radius, centerY - radius, centerX + radius,
            centerY + radius)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeInactiveColor
        canvas.drawArc(ovalRect, 180f, 180f, false, paint)
        paint.color = strokeActiveColor
        canvas.drawArc(ovalRect, 180f, 180f * boostValue, false, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (event.x < paddingLeft
                || event.x > measuredWidth - paddingRight
                || event.y < paddingTop
                || event.y > measuredHeight - paddingBottom) {
                return false
            }
            return true
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            val contentWidth = measuredWidth - paddingLeft - paddingRight
            val newBoostValue = ((event.x - paddingLeft) / contentWidth.toFloat()).coerceIn(0f, 1f)
            onBoostValueChangeListener?.onBoostValueChange(this, newBoostValue)
        }
        return super.onTouchEvent(event)
    }

    fun interface OnBoostValueChangeListener {
        fun onBoostValueChange(
            view: EffectBoosterView,
            @FloatRange(from = 0.0, to = 1.0) boostValue: Float
        )
    }
}