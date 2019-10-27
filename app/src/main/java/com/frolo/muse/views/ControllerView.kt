package com.frolo.muse.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar

import com.frolo.muse.R

import kotlinx.android.synthetic.main.include_controller.view.*


class ControllerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    var progress: Int
        get() = sb_controller.progress
        set(progress) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sb_controller.setProgress(progress, true)
        } else {
            sb_controller.progress = progress
        }

    init {
        orientation = LinearLayout.HORIZONTAL
        View.inflate(context, R.layout.include_controller, this)

        val label: String?
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ControllerView, 0, 0)
        try {
            label = ta.getString(R.styleable.ControllerView_label)
        } finally {
            ta.recycle()
        }
        tv_label.text = label
    }

    fun setOnSeekBarChangeListener(l: SeekBar.OnSeekBarChangeListener) {
        sb_controller.setOnSeekBarChangeListener(l)
    }

    fun setMax(max: Int) {
        sb_controller.max = max
    }
}
