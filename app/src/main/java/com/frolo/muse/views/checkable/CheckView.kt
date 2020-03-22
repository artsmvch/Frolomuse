package com.frolo.muse.views.checkable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import android.widget.Checkable
import androidx.annotation.FloatRange
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.frolo.muse.R
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt


/**
 * Animated check mark.
 * Origin: https://github.com/cdflynn/checkview
 */
class CheckView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr, R.style.Base_AppTheme_CheckView), Checkable {

    private val checkInterpolator: Interpolator = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PathInterpolator(0.755f, 0.05f, 0.855f, 0.06f)
        } else {
            AccelerateInterpolator()
        }
    }

    // helper tools
    private val pathCircle: Path = Path()
    private val pathCheck: Path = Path()
    private var minorContourLength: Float = 0.toFloat()
    private var majorContourLength: Float = 0.toFloat()

    private val drawingRect: RectF = RectF()
    private val circleRect: RectF = RectF()

    private val checkPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidth
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pathMeasure: PathMeasure = PathMeasure()
    private val point: FloatArray = FloatArray(2)
    private val checkStart: PointF = PointF()
    private val checkPivot: PointF = PointF()
    private val checkEnd: PointF = PointF()
    private val circleStart: PointF = PointF()

    // Animators
    private var checkAnimator: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null
    private var scaleAnimator: ValueAnimator? = null
    private var alphaAnimator: ValueAnimator? = null

    // internal state
    private var checked = false

    // public state
    var strokeWidth: Float = 0.toFloat()
        set(value) {
            field = value
            checkPaint.strokeWidth = value
            invalidate()
        }

    var strokeColor = DEFAULT_STROKE_COLOR
        set(value) {
            field = value
            checkPaint.color = value
            invalidate()
        }

    var circleBackgroundColor = DEFAULT_BCK_COLOR
        set(value) {
            field = value
            backgroundPaint.color = value
            invalidate()
        }

    init {
        if (attrs != null) {
            val arr = context.theme
                    .obtainStyledAttributes(attrs, R.styleable.CheckView, defStyleAttr, R.style.Base_AppTheme_CheckView)

            try {
                strokeWidth = arr.getDimension(
                        R.styleable.CheckView_strokeWidth,
                        dpToPx(DEFAULT_STROKE_WIDTH_IN_DP)
                )
                strokeColor = arr.getColor(R.styleable.CheckView_strokeColor, DEFAULT_STROKE_COLOR)
                circleBackgroundColor = arr.getColor(R.styleable.CheckView_circleColor, DEFAULT_BCK_COLOR)
            } finally {
                arr.recycle()
            }
        }

        checkPaint.also { paint ->
            paint.color = strokeColor
            paint.strokeWidth = strokeWidth
        }

        backgroundPaint.color = circleBackgroundColor
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            drawingRect.also { rect ->
                rect.left = paddingLeft.toFloat()
                rect.top = paddingTop.toFloat()
                rect.right = (measuredWidth - paddingRight).toFloat()
                rect.bottom = (measuredHeight - paddingBottom).toFloat()
            }

            checkStart.also { point ->
                point.x = drawingRect.left + drawingRect.width() / 4
                point.y = drawingRect.top + drawingRect.height() / 2
            }

            checkPivot.also { point ->
                point.x = drawingRect.left + drawingRect.width() * .426f
                point.y = drawingRect.top + drawingRect.height() * .66f
            }

            checkEnd.also { point ->
                point.x = drawingRect.left + drawingRect.width() * .75f
                point.y = drawingRect.top + drawingRect.height() * .30f
            }

            minorContourLength =
                    distance(checkStart.x, checkStart.y, checkPivot.x, checkPivot.y)
            majorContourLength =
                    distance(checkPivot.x, checkPivot.y, checkEnd.x, checkEnd.y)

            circleRect.also { rect ->
                rect.left = drawingRect.left + strokeWidth / 2
                rect.top = drawingRect.top + strokeWidth / 2
                rect.right = drawingRect.right - strokeWidth / 2
                rect.bottom = drawingRect.bottom - strokeWidth / 2
            }

            circleStart.also { point ->
                point.x = circleRect.right
                point.y = circleRect.bottom / 2
            }

            if (DEBUG && drawingRect.width() != drawingRect.height()) {
                // WARN: it's not a square
            }

            resolveStateImmediately()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!checked) {
            return
        }

        // background
        val width = (measuredWidth - paddingLeft - paddingRight).toFloat()
        val height = (measuredHeight - paddingTop - paddingBottom).toFloat()
        val cx = paddingLeft + width / 2f
        val cy = paddingTop + height / 2f
        val r = min(width, height) / 2f
        canvas.drawCircle(cx, cy, r, backgroundPaint)

        // check mark
        canvas.drawPath(pathCheck, checkPaint)
        canvas.drawPath(pathCircle, checkPaint)
    }

    override fun onDetachedFromWindow() {
        checkAnimator?.end()
        circleAnimator?.end()
        scaleAnimator?.end()
        alphaAnimator?.end()
        super.onDetachedFromWindow()
    }

    override fun isChecked(): Boolean {
        return checked
    }

    override fun setChecked(checked: Boolean) {
        setChecked(checked, true)
    }

    fun setChecked(checked: Boolean, animate: Boolean) {
        if (checked) {
            check(animate)
        } else {
            uncheck(animate)
        }
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    private fun check(animate: Boolean) {
        checked = true

        checkAnimator?.cancel()
        circleAnimator?.cancel()
        scaleAnimator?.cancel()
        alphaAnimator?.cancel()

        if (animate) {
            checkAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = CHECK_ANIM_DURATION
                interpolator = checkInterpolator
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    setCheckPathPercentage(fraction)
                    invalidate()
                }
                start()
            }

            circleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = CHECK_ANIM_DURATION
                interpolator = checkInterpolator
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    setCirclePathPercentage(fraction)
                    invalidate()
                }
                start()
            }

            scaleAnimator = ValueAnimator.ofFloat(1f, SCALE_MIN, 1f).apply {
                duration = SCALE_ANIM_DURATION
                startDelay = SCALE_ANIM_DELAY
                interpolator = FastOutSlowInInterpolator()
                addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    scaleX = value
                    scaleY = value
                    invalidate()
                }
                start()
            }

            alphaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                removeAllUpdateListeners()
                duration = ALPHA_ANIM_DURATION
                addUpdateListener { animation ->
                    val value = animation.animatedValue as Float
                    alpha = value
                    invalidate()
                }
                start()
            }
        } else {
            setCheckPathPercentage(1f)
            setCirclePathPercentage(1f)
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
            invalidate()
        }
    }

    private fun uncheck(animate: Boolean) {
        checked = false
        invalidate()
    }

    private fun resolveStateImmediately() {
        if (checked) {
            setCheckPathPercentage(1f)
            setCirclePathPercentage(1f)
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }
    }

    private fun setCheckPathFull() {
        pathCheck.apply {
            reset()
            moveTo(checkStart.x, checkStart.y)
            lineTo(checkPivot.x, checkPivot.y)
            lineTo(checkEnd.x, checkEnd.y)
        }
    }

    private fun setCheckPathPercentage(@FloatRange(from = 0.0, to = 1.0) percent: Float) {
        setCheckPathFull()
        val totalLength = minorContourLength + majorContourLength
        val pivotPercent = minorContourLength / totalLength

        when {
            percent > pivotPercent -> {
                val remainder = percent - pivotPercent
                val distance = totalLength * remainder

                pathCheck.apply {
                    reset()
                    moveTo(checkPivot.x, checkPivot.y)
                    lineTo(checkEnd.x, checkEnd.y)
                }

                pathMeasure.apply {
                    setPath(pathCheck, false)
                    getPosTan(distance, point, null)
                }

                pathCheck.apply {
                    reset()
                    moveTo(checkStart.x, checkStart.y)
                    lineTo(checkPivot.x, checkPivot.y)
                    lineTo(point[0], point[1])
                }
            }
            percent < pivotPercent -> {
                val minorPercent = percent / pivotPercent
                val distance = minorContourLength * minorPercent

                pathMeasure.apply {
                    setPath(pathCheck, false)
                    getPosTan(distance, point, null)
                }

                pathCheck.apply {
                    reset()
                    moveTo(checkStart.x, checkStart.y)
                    lineTo(point[0], point[1])
                }
            }
            percent == pivotPercent -> pathCheck.apply {
                lineTo(checkPivot.x, checkPivot.y)
            }
        }
    }

    private fun setCirclePathPercentage(@FloatRange(from = 0.0, to = 1.0) percent: Float) {
        pathCircle.apply {
            reset()
            moveTo(circleStart.x, circleStart.y)
            addArc(circleRect, 0f, 360f)
        }

        pathMeasure.apply {
            setPath(pathCircle, false)
        }

        val distance = pathMeasure.length * percent

        pathMeasure.apply {
            getPosTan(distance, point, null)
        }

        pathCircle.apply {
            reset()
            moveTo(circleStart.x, circleStart.y)
            arcTo(circleRect, 0f, 359 * percent)
        }
    }

    private fun dpToPx(dp: Float): Float {
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    companion object {
        private const val DEBUG = false
        private const val CHECK_ANIM_DURATION = 300L
        private const val SCALE_ANIM_DELAY = 280L
        private const val SCALE_ANIM_DURATION = 250L
        private const val ALPHA_ANIM_DURATION = 350L
        private const val DEFAULT_STROKE_WIDTH_IN_DP = 2f
        private const val DEFAULT_STROKE_COLOR = -0xe55500
        private const val DEFAULT_BCK_COLOR = -0x1
        private const val SCALE_MIN = 0.80f

        private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val xAbs = abs(x1 - x2)
            val yAbs = abs(y1 - y2)
            return sqrt((yAbs * yAbs + xAbs * xAbs).toDouble()).toFloat()
        }
    }
}