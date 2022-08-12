package com.frolo.muse.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.frolo.muse.R


class PlaceholderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    private val iconImageView: ImageView
    private val messageTextView: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        View.inflate(context, R.layout.merge_placeholder, this)
        iconImageView = findViewById(R.id.imv_icon)
        messageTextView = findViewById(R.id.tv_message)
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PlaceholderView, defStyleAttr, 0)
        try {
            ta.getDrawable(R.styleable.PlaceholderView_image)?.apply {
                iconImageView.setImageDrawable(this)
            }

            ta.getString(R.styleable.PlaceholderView_message)?.apply {
                messageTextView.text = this
            }
        } finally {
            ta.recycle()
        }
    }

}