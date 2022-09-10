package com.frolo.audiofx2.ui.view

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
import com.frolo.audiofx2.ui.R
import com.frolo.ui.Screen
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


class EffectBoosterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.effectBoosterViewStyle
): View(context, attrs, defStyleAttr,  R.style.EffectBoosterView_Default) {

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

    // Measurable properties
    private var arcCenterX: Float = 0f
    private var arcCenterY: Float = 0f
    private var arcRadius: Float = 0f
    private val arcRect: RectF = RectF()
//    private var pointerCenterX: Float = 0f
//    private var pointerCenterY: Float = 0f
    private val pointerRadius: Float = Screen.dpFloat(context, 8f)

    // Drawing tools
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

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

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        invalidate()
    }

    override fun getSuggestedMinimumWidth(): Int {
        return Screen.dp(context, DEFAULT_WIDTH)
    }

    override fun getSuggestedMinimumHeight(): Int {
        return Screen.dp(context, DEFAULT_HEIGHT)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val defaultWidth = Screen.dp(context, DEFAULT_WIDTH)
        val defaultHeight = Screen.dp(context, DEFAULT_HEIGHT)

        val width: Int
        val height: Int
        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                height = when (heightMode) {
                    MeasureSpec.UNSPECIFIED -> defaultHeight
                    MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
                    MeasureSpec.AT_MOST -> {
                        min(MeasureSpec.getSize(heightMeasureSpec), defaultHeight)
                    }
                    else -> defaultHeight
                }
                width = 2 * height
            }
            MeasureSpec.EXACTLY -> {
                width = MeasureSpec.getSize(widthMeasureSpec)
                height = when (heightMode) {
                    MeasureSpec.UNSPECIFIED -> width / 2
                    MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
                    MeasureSpec.AT_MOST -> {
                        min(MeasureSpec.getSize(heightMeasureSpec), width / 2)
                    }
                    else -> defaultHeight
                }
            }
            MeasureSpec.AT_MOST -> {
                when (heightMode) {
                    MeasureSpec.UNSPECIFIED -> {
                        width = min(MeasureSpec.getSize(widthMeasureSpec), defaultWidth)
                        height = width / 2
                    }
                    MeasureSpec.EXACTLY -> {
                        height = MeasureSpec.getSize(heightMeasureSpec)
                        width = min(MeasureSpec.getSize(widthMeasureSpec), defaultWidth)
                    }
                    MeasureSpec.AT_MOST -> {
                        width = min(MeasureSpec.getSize(widthMeasureSpec),
                            2 * MeasureSpec.getSize(heightMeasureSpec))
                        height = width / 2
                    }
                    else -> {
                        width = defaultWidth
                        height = defaultHeight
                    }
                }
            }
            else -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                return
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val contentWidth = measuredWidth - paddingLeft - paddingRight
        val contentHeight = measuredHeight - paddingTop - paddingBottom
        val offset = max(strokeWidth / 2f, pointerRadius)
        this.arcRadius = min(contentWidth / 2, contentHeight).toFloat() - 2 * offset
        this.arcCenterX = paddingLeft + contentWidth / 2f
        this.arcCenterY = measuredHeight - paddingBottom.toFloat() - offset
        arcRect.set(arcCenterX - arcRadius, arcCenterY - arcRadius,
            arcCenterX + arcRadius, arcCenterY + arcRadius)
    }

    override fun onDraw(canvas: Canvas) {
        // Drawing inactive stroke
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = strokeInactiveColor
        canvas.drawArc(arcRect, 180f, 180f, false, paint)

        // Drawing active stroke
        paint.color = if (isEnabled) {
            strokeActiveColor
        } else {
            strokeInactiveColor
        }
        canvas.drawArc(arcRect, 180f, 180f * boostValue, false, paint)

        // Drawing pointer
        val degrees: Float = 180f + 180f * boostValue
        val pointerCenterX = arcCenterX + arcRadius * cos(degrees * Math.PI / 180).toFloat()
        val pointerCenterY = arcCenterY + arcRadius * sin(degrees * Math.PI / 180).toFloat()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(pointerCenterX, pointerCenterY, pointerRadius, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (event.x < arcRect.left
                || event.x > arcRect.right
                || event.y < arcRect.top
                || event.y > arcRect.bottom) {
                return false
            }
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val newBoostValue = ((event.x - arcRect.left) / arcRect.width()).coerceIn(0f, 1f)
            this.boostValue = newBoostValue
            onBoostValueChangeListener?.onBoostValueChange(this, newBoostValue)
            return true
        }
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            parent?.requestDisallowInterceptTouchEvent(false)
        }
        return super.onTouchEvent(event)
    }

    fun interface OnBoostValueChangeListener {
        fun onBoostValueChange(
            view: EffectBoosterView,
            @FloatRange(from = 0.0, to = 1.0) boostValue: Float
        )
    }

    companion object {
        private const val DEFAULT_HEIGHT = 120
        private const val DEFAULT_WIDTH = 2 * DEFAULT_HEIGHT
    }
}