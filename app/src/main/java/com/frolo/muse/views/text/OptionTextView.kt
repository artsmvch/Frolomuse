package com.frolo.muse.views.text

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.frolo.muse.R
import kotlinx.android.synthetic.main.merge_option_text_view.view.*


class OptionTextView @JvmOverloads constructor(
    context: Context, attrs:
    AttributeSet? = null,
    defStyleAttr: Int = R.attr.optionTextViewStyle,
    defStyleRes: Int = R.style.Base_AppTheme_OptionTextView
): LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var iconScaleAnim: Animator? = null
    private var iconScale: Float = 1f
        set(value) {
            field = value
            imv_icon.scaleX = value
            imv_icon.scaleY = value
        }

    var optionTitle: String? = null
        set(value) {
            field = value
            tv_title.text = value
        }

    var optionIcon: Drawable? = null
        set(value) {
            field = value
            imv_icon.setImageDrawable(value)
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        View.inflate(context, R.layout.merge_option_text_view, this)

        val arr = context.theme.obtainStyledAttributes(
                attrs, R.styleable.OptionTextView, defStyleAttr, defStyleRes)

        optionTitle = arr.getString(R.styleable.OptionTextView_optionTitle)
        optionIcon = arr.getDrawable(R.styleable.OptionTextView_optionIcon)

        arr.recycle()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action

        if (action == MotionEvent.ACTION_DOWN) {
            animatePressDown()
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            animatePressUp()
        }

        return super.onTouchEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        iconScale = 1f
    }

    override fun onDetachedFromWindow() {
        iconScaleAnim?.cancel()
        super.onDetachedFromWindow()
    }

    private fun animatePressDown() {
        iconScaleAnim?.cancel()

        iconScaleAnim = ValueAnimator.ofFloat(iconScale, 0.8f).apply {
            interpolator = SCALE_ANIM_INTERPOLATOR
            duration = 250
            addUpdateListener {
                iconScale = it.animatedValue as Float
            }
            start()
        }
    }

    private fun animatePressUp() {
        iconScaleAnim?.cancel()

        iconScaleAnim = ValueAnimator.ofFloat(iconScale, 1f).apply {
            interpolator = SCALE_ANIM_INTERPOLATOR
            duration = 50
            addUpdateListener {
                iconScale = it.animatedValue as Float
            }
            start()
        }
    }

    companion object {
        private val SCALE_ANIM_INTERPOLATOR = FastOutSlowInInterpolator()
    }

}