package com.frolo.muse.views.equalizer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.muse.engine.AudioFx


class EqualizerLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var minEqBandLevelRange: Short = 0
    private var onBandLevelChangeListener: OnBandLevelChangeListener? = null

    fun setOnBandLevelChangeListener(listener: OnBandLevelChangeListener) {
        this.onBandLevelChangeListener = listener
    }

    interface OnBandLevelChangeListener {
        fun onStartTrackingBandLevel(band: Short, level: Short)
        fun onBandLevelChange(band: Short, level: Short)
        fun onStopTrackingBandLevel(band: Short, level: Short)
    }

    fun getLevels(): ShortArray {
        val levels = mutableListOf<Short>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val barView = child.findViewById<BandBar>(R.id.band_bar)
            //val pos = barView.tag as Short
            val level = (minEqBandLevelRange + barView.progress).toShort()
            levels.add(level)
        }
        return levels.toShortArray()
    }

    @SuppressLint("DefaultLocale")
    fun bindWith(audioFx: AudioFx, animate: Boolean) {
        //rebuild(audioFx)
        val container = this
        //container.removeAllViews() // removing all view before adding new ones
        val res = resources
        val numberOfBands = audioFx.getNumberOfBands().toInt()

        // init one freq bar
        val minEqBandLevelRange = audioFx.getMinBandLevelRange() // lower
        val maxEqBandLevelRange = audioFx.getMaxBandLevelRange() // upper
        val diff = maxEqBandLevelRange - minEqBandLevelRange
        this.minEqBandLevelRange = minEqBandLevelRange

        var addedBandsCount = 0
        for (i in 0 until numberOfBands) {
            val band = i.toShort()

            val ranges = audioFx.getBandFreqRange(band)
            if (ranges.size != 2) { // invalid ranges
                continue
            }

            if (i == numberOfBands - 1) { // if it`s the last band
                if (ranges[1] == 0) {
                    ranges[1] = 10000000 // because it is 0 sometimes
                }
            }
            val min = 0 // ranges[0]
            val curr = audioFx.getBandLevel(band) - minEqBandLevelRange

            val barWrapperView = if (i >= container.childCount)  {
                createNewBarWrapperView(band).also {
                    container.addView(it)
                }
            } else {
                container.getChildAt(i)
            }
            addedBandsCount++
            val barView = barWrapperView.findViewById<BandBar>(R.id.band_bar)
            barView.setOnBandLevelChangeListener(null)
            val upperRangeTextView = barWrapperView.findViewById<TextView>(R.id.tv_upper_border)
            val lowerRangeTextView = barWrapperView.findViewById<TextView>(R.id.tv_lower_border)
            upperRangeTextView.text = res.getString(R.string.freq_range, (ranges[1] / 1000).toString() + "k")
            lowerRangeTextView.text = res.getString(R.string.freq_range, (ranges[0] / 1000).toString() + "k")

            barView.min = min
            barView.max = diff
            barView.setProgress(curr, animate)
            barView.tag = band
            val listener = object : BandBar.OnBandLevelChangeListener() {
                override fun onStartTrackingTouch(bar: BandBar) {
                    val pos = bar.tag as Short
                    val level = (minEqBandLevelRange + bar.progress).toShort()
                    onBandLevelChangeListener?.onStopTrackingBandLevel(pos, level)
                }

                override fun onStopTrackingTouch(bar: BandBar) {
                    val pos = bar.tag as Short
                    val level = (minEqBandLevelRange + bar.progress).toShort()
                    onBandLevelChangeListener?.onStopTrackingBandLevel(pos, level)
                }

                override fun onBandLevelChanged(bar: BandBar, progress: Int, fromUser: Boolean) {
                    val pos = bar.tag as Short
                    val level = (minEqBandLevelRange + bar.progress).toShort()
                    onBandLevelChangeListener?.onBandLevelChange(pos, level)
                }
            }
            barView.setOnBandLevelChangeListener(listener)
        }

        // removing views those weren't bounded to any band
        while (addedBandsCount < container.childCount) {
            container.removeViewAt(container.childCount - 1)
        }
    }

    /**
     * Creates a new bar wrapper view for the given band (band index);
     */
    private fun createNewBarWrapperView(band: Short): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.include_eq_bar, this, false).apply {
            val lp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            lp.weight = 1f
            lp.gravity = Gravity.CENTER
            layoutParams = lp
        }
    }
}
