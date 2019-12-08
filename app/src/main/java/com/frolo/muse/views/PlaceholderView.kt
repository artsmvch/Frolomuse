package com.frolo.muse.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.frolo.muse.R
import kotlinx.android.synthetic.main.merge_placeholder.view.*


class PlaceholderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        View.inflate(context, R.layout.merge_placeholder, this)
    }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PlaceholderView, 0, 0)
        try {
            ta.getDrawable(R.styleable.PlaceholderView_image)?.apply {
                imv_icon.setImageDrawable(this)
            }

            ta.getString(R.styleable.PlaceholderView_message)?.apply {
                tv_message.text = this
            }
        } finally {
            ta.recycle()
        }
    }

}