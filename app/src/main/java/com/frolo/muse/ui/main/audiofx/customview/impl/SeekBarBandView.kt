package com.frolo.muse.ui.main.audiofx.customview.impl

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.ui.main.audiofx.customview.BaseEqualizerView
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar


/**
 * Band view based on vertical [SeekBar]. Migrate to [com.google.android.material.slider.Slider]
 * when it gets the vertical version.
 */
class SeekBarBandView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr), BaseEqualizerView.BandView {

    private val isDebug = BuildConfig.DEBUG

    private val listeners = HashSet<BaseEqualizerView.BandListener>(1)

    // Animation state
    private var levelAnimator: ValueAnimator? = null
    private val levelAnimationInterpolator = AccelerateDecelerateInterpolator()
    private val levelAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animator ->
        val animatedLevel = (animator.animatedValue as Int) - minLevel
        verticalSeekBar.progress = animatedLevel
        dispatchAnimatedValueChanged(animatedLevel)
    }

    // Internal state
    private var minLevel = 0
    private var maxLevel = 1
    private var currentLevel = 0

    // Internal views
    private val labelTextView: TextView
    private val verticalSeekBarWrapper: View
    private val verticalSeekBar: VerticalSeekBar

    private val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                val newValue = minLevel + progress
                currentLevel = newValue
                dispatchValueChangedByUser(newValue)
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    }

    override val actualLevel: Int get() = currentLevel
    override val animatedLevel: Int get() = (levelAnimator?.animatedValue as? Int) ?: currentLevel
    override val thumbCenterX: Float get() = calculateThumbCenterX()
    override val thumbCenterY: Float get() = calculateThumbCenterY()
    override val centerY: Float get() = calculateCenterY()

    init {
        super.setOrientation(VERTICAL)
        gravity = Gravity.CENTER

        View.inflate(context, R.layout.merge_seek_bar_band_view, this)

        labelTextView = findViewById(R.id.tv_label)
        verticalSeekBarWrapper = findViewById(R.id.vertical_seek_bar_wrapper)
        verticalSeekBar = findViewById(R.id.vertical_seek_bar)
        verticalSeekBar.rotationAngle = VerticalSeekBar.ROTATION_ANGLE_CW_270
        verticalSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
    }

    private fun dispatchAnimatedValueChanged(animatedLevel: Int) {
        listeners.forEach { listener ->
            listener.onAnimatedLevelChanged(this, animatedLevel)
        }
    }

    private fun dispatchValueChangedByUser(level: Int) {
        listeners.forEach { listener ->
            listener.onLevelChanged(this, level)
        }
    }

    override fun setLevelRange(minLevel: Int, maxLevel: Int) {
        if (isDebug) {
            require(minLevel < maxLevel) {
                "Invalid range: min=$minLevel, maxLevel=$maxLevel"
            }
        }

        val newLevelValue = clamp(actualLevel, minLevel, maxLevel)

        this.minLevel = minLevel
        this.maxLevel = maxLevel
        this.currentLevel = newLevelValue

        val newRange = maxLevel - minLevel
        verticalSeekBar.max = newRange

        val targetSeekBarValue = newLevelValue - minLevel
        verticalSeekBar.progress = targetSeekBarValue
    }

    override fun setLevel(level: Int, animate: Boolean) {
        if (currentLevel == level) {
            // no changes
            return
        }

        // Getting the current animated level value according to the running animation.
        // If there is no running animation, then just the currently stored level value is taken.
        val currentAnimatedValue = levelAnimator?.animatedValue as? Int ?: currentLevel

        // Cancelling the previous animation, if any
        levelAnimator?.cancel()
        levelAnimator = null

        // Updating the actual value
        currentLevel = level

        if (animate) {
            val newAnimator = ValueAnimator.ofInt(currentAnimatedValue, level).apply {
                duration = ANIMATION_DURATION
                interpolator = levelAnimationInterpolator
                addUpdateListener(levelAnimatorUpdateListener)
                doOnEnd { animator ->
                    if (levelAnimator == animator) {
                        levelAnimator = null
                    }
                }
            }
            newAnimator.start()
            levelAnimator = newAnimator
        } else {
            // Immediately setting progress to the seek bar
            verticalSeekBar.progress = level - minLevel
        }
    }

    override fun setLabel(label: CharSequence) {
        labelTextView.text = label
    }

    override fun setTrackTint(@ColorInt color: Int) {
        val colorStateList = ColorStateList.valueOf(color)
        val mode = PorterDuff.Mode.SRC

        verticalSeekBar.progressBackgroundTintList = colorStateList
        verticalSeekBar.progressBackgroundTintMode = mode

        verticalSeekBar.progressTintList = colorStateList
        verticalSeekBar.progressTintMode = mode

        verticalSeekBar.secondaryProgressTintList = colorStateList
        verticalSeekBar.secondaryProgressTintMode = mode
    }

    override fun setThumbTint(@ColorInt color: Int) {
        verticalSeekBar.thumbTintList = ColorStateList.valueOf(color)
        //verticalSeekBar.thumbTintMode = PorterDuff.Mode.SRC_OVER
    }

    override fun registerListener(listener: BaseEqualizerView.BandListener) {
        listeners.add(listener)
    }

    override fun unregisterListener(listener: BaseEqualizerView.BandListener) {
        listeners.remove(listener)
    }

    override fun unregisterAllListeners() {
        listeners.clear()
    }

    private fun calculateThumbCenterX(): Float {
        val width: Int = verticalSeekBarWrapper.measuredWidth
                - verticalSeekBarWrapper.paddingLeft
                - verticalSeekBarWrapper.paddingRight
        return verticalSeekBarWrapper.left + verticalSeekBarWrapper.paddingLeft + width / 2f
    }

    private fun calculateThumbCenterY(): Float {
        val progressPercentage: Float =
                (verticalSeekBar.max - verticalSeekBar.progress).toFloat() / verticalSeekBar.max
        return calculateYForProgress(progressPercentage)
    }

    private fun calculateCenterY(): Float {
        return calculateYForProgress(0.5f)
    }

    private fun calculateYForProgress(progressPercentage: Float): Float {
        val thumb: Drawable? = verticalSeekBar.thumb
        val thumbSize = thumb?.intrinsicWidth ?: 0
        val trackHeight: Int = ((verticalSeekBar.measuredWidth
                - verticalSeekBar.paddingLeft
                - verticalSeekBar.paddingRight
                - thumbSize)
                + 2 * verticalSeekBar.thumbOffset)
        val progressedTrackHeight = (trackHeight * progressPercentage).toInt()
        return (verticalSeekBarWrapper.top
                + verticalSeekBar.top + verticalSeekBar.paddingTop
                + verticalSeekBar.thumbOffset * 2f + progressedTrackHeight)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        levelAnimator?.end()
        levelAnimator = null
    }

    companion object {
        private const val ANIMATION_DURATION = 250L

        private fun clamp(value: Int, min: Int, max: Int): Int {
            return value.coerceIn(min, max)
        }
    }

}