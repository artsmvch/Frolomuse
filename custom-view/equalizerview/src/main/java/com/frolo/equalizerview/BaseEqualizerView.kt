package com.frolo.equalizerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.ColorUtils
import androidx.core.view.forEach
import java.util.*


@Suppress("UNCHECKED_CAST")
abstract class BaseEqualizerView<V> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.equalizerViewStyle,
    defStyleRes: Int = DEFAULT_STYLE_RES_ID
): FrameLayout(context, attrs, defStyleAttr) where V: View, V: BaseEqualizerView.BandView {

    private val equalizerObserver: IEqualizer.Observer = object : IEqualizer.Observer {
        override fun onBandLevelChanged(band: Short, level: Short) {
            val container = getBandViewContainer()
            if (band >= 0 && band < container.childCount) {
                val bandView = container.getChildAt(band.toInt()) as V
                bandView.setLevel(level.toInt(), true)
            }
        }
    }

    protected var equalizer: IEqualizer? = null
        private set

    private val childContext: Context
    private val bandsContainer: LinearLayout by lazy {
        LinearLayout(context).also { layout ->
            layout.orientation = LinearLayout.HORIZONTAL
            addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
    }

    private val drawVisuals: Boolean

    var gridLineThickness = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    @ColorInt
    var gridColor: Int = DEFAULT_GRID_COLOR
        set(value) {
            if (field != value) {
                field = value
                if (isEqualizerUiEnabled) {
                    setAllTracksTint(value)
                }
                invalidate()
            }
        }

    @ColorInt
    var levelColor: Int = DEFAULT_LEVEL_COLOR
        set(value) {
            if (field != value) {
                field = value
                if (isEqualizerUiEnabled) {
                    setAllThumbsTint(value)
                }
                invalidate()
            }
        }

    @ColorInt
    var disableColor: Int = DEFAULT_DISABLE_COLOR
        set(value) {
            if (field != value) {
                field = value
                if (!isEqualizerUiEnabled) {
                    setAllTracksTint(value)
                    setAllThumbsTint(value)
                }
                invalidate()
            }
        }

    // Background visual tools
    private val visualPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val visualNeutralPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val visualPaths: List<VisualPath>

    /**
     * Returns the current levels set by user.
     * @return current levels
     */
    val currentLevels: ShortArray
        get() {
            val container = getBandViewContainer()
            val numberOfBands = container.childCount
            val levels = ShortArray(numberOfBands)
            for (i in 0 until numberOfBands) {
                val bandView = container.getChildAt(i) as BandView
                levels[i] = bandView.actualLevel.toShort()
            }
            return levels
        }

    var isEqualizerUiEnabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                setAllTracksTint(
                    tint = if (value) gridColor else disableColor
                )
                setAllThumbsTint(
                    tint = if (value) levelColor else disableColor
                )
                invalidate()
            }
        }

    init {
        val styleId = attrs?.getAttributeIntValue(null, "style", DEFAULT_STYLE_RES_ID)
                ?: DEFAULT_STYLE_RES_ID
        childContext = ContextThemeWrapper(context, styleId)

        val a = context.theme
                .obtainStyledAttributes(attrs, R.styleable.BaseEqualizerView, defStyleAttr, defStyleRes)
        try {
            drawVisuals = a.getBoolean(R.styleable.BaseEqualizerView_drawVisuals, false)
            gridLineThickness = a.getDimension(R.styleable.BaseEqualizerView_gridLineThickness, 0f)
            gridColor = a.getColor(R.styleable.BaseEqualizerView_gridColor, DEFAULT_GRID_COLOR)
            levelColor = a.getColor(R.styleable.BaseEqualizerView_levelColor, DEFAULT_LEVEL_COLOR)
            disableColor = a.getColor(R.styleable.BaseEqualizerView_disableColor, DEFAULT_DISABLE_COLOR)
        } finally {
            a.recycle()
        }

        visualPaint.style = Paint.Style.STROKE
        visualPaint.strokeWidth = dpToPx(context, 2f)
        visualNeutralPaint.strokeWidth = dpToPx(context, 1.6f)

        visualPaths = ArrayList(3)
        visualPaths.add(VisualPath(alpha = 102, strokeWidth = dpToPx(context, 3f)))
        visualPaths.add(VisualPath(alpha = 78, strokeWidth = dpToPx(context, 1.2f)))
        visualPaths.add(VisualPath(alpha = 48, strokeWidth = dpToPx(context, 1f)))

        @Suppress("LeakingThis")
        setWillNotDraw(!drawVisuals)
    }

    private fun getBandViewContainer(): ViewGroup {
        return bandsContainer
    }

    private fun setAllTracksTint(@ColorInt tint: Int) {
        getBandViewContainer().forEach { child ->
            (child as BandView).setTrackTint(tint)
        }
    }

    private fun setAllThumbsTint(@ColorInt tint: Int) {
        getBandViewContainer().forEach { child ->
            (child as BandView).setThumbTint(tint)
        }
    }

    /**
     * Setups the view with the given equalizer.
     * @param equalizer to bind with
     * @param animate if true, then the changes will be animated
     */
    @JvmOverloads
    fun setup(equalizer: IEqualizer?, animate: Boolean = true) {

        val oldEqualizer = this.equalizer
        oldEqualizer?.unregisterObserver(equalizerObserver)

        this.equalizer = equalizer

        val container = getBandViewContainer()
        if (equalizer == null) {
            // No equalizer - no band views
            container.removeAllViews()
            return
        }

        if (isAttachedToWindow) {
            equalizer.registerObserver(equalizerObserver)
        }

        val numberOfBands = equalizer.numberOfBands
        val minBandLevel = equalizer.minBandLevelRange
        val maxBandLevel = equalizer.maxBandLevelRange

        var addedBandCount = 0
        for (bandIndex in 0 until numberOfBands) {

            val bandView = if (bandIndex >= container.childCount) {
                val newBandView = onCreateBandView()
                val layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT)
                layoutParams.weight = 1f
                newBandView.layoutParams = layoutParams
                container.addView(newBandView, container.childCount)
                newBandView
            } else {
                @Suppress("UNCHECKED_CAST")
                container.getChildAt(bandIndex) as V
            }

            addedBandCount++

            val currentLevel = equalizer.getBandLevel(bandIndex.toShort()).toInt()
            val frequencyRange = equalizer.getBandFreqRange(bandIndex.toShort())

            val listener = object : BandListener {
                override fun onLevelChanged(bandView: BandView, level: Int) {
                    invalidate()
                    onDispatchLevelChange(bandIndex, level)
                }

                override fun onAnimatedLevelChanged(bandView: BandView, animatedLevel: Int) {
                    invalidate()
                }
            }

            bandView.registerListener(listener)

            bandView.setLevelRange(minBandLevel, maxBandLevel)
            bandView.setLevel(currentLevel, animate)
            bandView.setLabel(getBandLabel(bandIndex, frequencyRange))
            bandView.setTrackTint(gridColor)
            bandView.setThumbTint(levelColor)
            bandView.setTag(R.id.tag_band_index, bandIndex)
        }

        // Removing views that are not bound to any band
        while (addedBandCount < container.childCount) {
            val childIndex = container.childCount - 1
            (container.getChildAt(childIndex) as V).unregisterAllListeners()
            container.removeViewAt(childIndex)
        }
    }

    /**
     * Creates a new band view.
     */
    protected abstract fun onCreateBandView(): V

    /**
     * Dispatches the level change of the band at [bandIndex].
     * Inheritors must set the new [level] value for the current equalizer.
     * Additional throttling can be applied for this action.
     */
    protected abstract fun onDispatchLevelChange(bandIndex: Int, level: Int)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val currEqualizer: IEqualizer? = equalizer
        if (currEqualizer != null) {
            currEqualizer.registerObserver(equalizerObserver)
            val container = getBandViewContainer()
            val numberOfBands = currEqualizer.numberOfBands.toInt()
            val viewChildCount = container.childCount
            // Actually, the number of bands must be equal to the child count
            for (i in 0 until numberOfBands.coerceAtMost(viewChildCount)) {
                val level = currEqualizer.getBandLevel(i.toShort()).toInt()
                val bandView = container.getChildAt(i) as BandView
                bandView.setLevel(level, false)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        equalizer?.unregisterObserver(equalizerObserver)
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (drawVisuals) {
            preDrawVisuals(canvas)
        }

        super.dispatchDraw(canvas)

        if (drawVisuals) {
            postDrawVisuals(canvas)
        }
    }

    /**
     * Draws visuals under the band views.
     */
    private fun preDrawVisuals(canvas: Canvas) {
        val container = getBandViewContainer()
        val bandViewCount = container.childCount
        if (bandViewCount < 1) {
            // Nothing to draw
            return
        }

        val firstBandView = container.getChildAt(0) as V

        // The y of the center
        val neutralY = container.top + container.paddingTop + firstBandView.centerY

        // The centered horizontal line
        visualNeutralPaint.strokeWidth = gridLineThickness
        visualNeutralPaint.color = if (isEqualizerUiEnabled) {
            gridColor
        } else {
            disableColor
        }
        canvas.drawLine(paddingLeft.toFloat(), neutralY,
                measuredWidth - paddingRight.toFloat(), neutralY, visualNeutralPaint)
    }

    /**
     * Draws visuals over the band views.
     */
    private fun postDrawVisuals(canvas: Canvas) {
        val container = getBandViewContainer()
        val bandViewCount = container.childCount
        if (bandViewCount < 1) {
            // Nothing to draw
            return
        }

        val firstBandView = container.getChildAt(0) as V

        val topOffset = container.top + container.paddingTop

        // The y of the center
        val neutralY = firstBandView.centerY

        // Visual paths
        for (visualPath in visualPaths) {
            visualPath.path.reset()
            visualPath.tmpCx1 = paddingLeft.toFloat()
            visualPath.tmpCy1 = topOffset + neutralY
            visualPath.path.moveTo(visualPath.tmpCx1, visualPath.tmpCy1)
        }
        for (i in 0..bandViewCount) {
            for (visualPathIndex in visualPaths.indices) {
                val visualPath = visualPaths[visualPathIndex]
                val yCoefficient = when (visualPathIndex) {
                    0 -> 0f
                    1 -> 0.2f
                    2 -> 0.3f
                    else -> 0.5f
                }
                if (i < bandViewCount) {
                    val bandView = container.getChildAt(i) as V
                    val centerY = bandView.thumbCenterY
                    visualPath.tmpCx2 = bandView.left + bandView.thumbCenterX
                    visualPath.tmpCy2 = topOffset + bandView.top + centerY + (neutralY - centerY) * yCoefficient
                } else {
                    visualPath.tmpCx2 = measuredWidth - paddingRight.toFloat()
                    visualPath.tmpCy2 = topOffset + neutralY
                }
                val x1 = visualPath.tmpCx1 + (visualPath.tmpCx2 - visualPath.tmpCx1) / 2f
                val y1 = visualPath.tmpCy1
                val x2 = visualPath.tmpCx1 + (visualPath.tmpCx2 - visualPath.tmpCx1) / 2f
                val y2 = visualPath.tmpCy2
                visualPath.path.cubicTo(x1, y1, x2, y2, visualPath.tmpCx2, visualPath.tmpCy2)
                visualPath.tmpCx1 = visualPath.tmpCx2
                visualPath.tmpCy1 = visualPath.tmpCy2
            }
        }

        for (visualPath in visualPaths) {
            val color = if (isEqualizerUiEnabled) {
                levelColor
            } else {
                disableColor
            }
            visualPaint.color = ColorUtils.setAlphaComponent(color, visualPath.alpha)
            visualPaint.strokeWidth = visualPath.strokeWidth
            canvas.drawPath(visualPath.path, visualPaint)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isEqualizerUiEnabled) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEqualizerUiEnabled) {
            return true
        }
        return super.onTouchEvent(event)
    }

    /**
     * Internal state of a visual path.
     */
    private class VisualPath constructor(
        @IntRange(from = 0, to = 255)
        val alpha: Int,
        val strokeWidth: Float
    ) {
        val path: Path = Path()

        // tmp values used to calculate pixel positions while drawing
        var tmpCx1 = 0f
        var tmpCy1 = 0f
        var tmpCx2 = 0f
        var tmpCy2 = 0f
    }

    interface BandView {
        val actualLevel: Int
        val animatedLevel: Int

        val thumbCenterX: Float
        val thumbCenterY: Float
        val centerY: Float

        fun setLevelRange(minLevel: Int, maxLevel: Int)
        fun setLevel(level: Int, animate: Boolean)

        fun setLabel(label: CharSequence)

        fun setTrackTint(@ColorInt color: Int)
        fun setThumbTint(@ColorInt color: Int)

        fun registerListener(listener: BandListener)
        fun unregisterListener(listener: BandListener)
        fun unregisterAllListeners()
    }

    interface BandListener {
        /**
         * Called when the actual level of the band has been changed by the user.
         */
        fun onLevelChanged(bandView: BandView, level: Int)

        /**
         * Called when the animated level of the band has been changed due to animation update.
         * NOTE: [animatedLevel] is not the actual value of the band level.
         */
        fun onAnimatedLevelChanged(bandView: BandView, animatedLevel: Int)
    }

    companion object {
        private val DEFAULT_STYLE_RES_ID = R.style.EqualizerView_Default
        private const val DEFAULT_GRID_COLOR = Color.TRANSPARENT
        private const val DEFAULT_LEVEL_COLOR = Color.LTGRAY
        private const val DEFAULT_DISABLE_COLOR = Color.LTGRAY

        private fun getBandLabel(bandIndex: Int, frequencyRange: IntArray): String {
            val freq: Int = frequencyRange.getOrNull(0) ?: 0
            return when {
                //freq > 1_000_000 -> (freq / 1_000_000).toString() + "\nkkHz"
                else -> (freq / 1000).toString() + "+"
            }
        }

        private fun dpToPx(context: Context, dp: Float): Float {
            return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}