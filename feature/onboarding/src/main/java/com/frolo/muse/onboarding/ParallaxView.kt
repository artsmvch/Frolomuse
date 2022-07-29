package com.frolo.muse.onboarding

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.annotation.FloatRange
import androidx.core.view.doOnLayout


internal class ParallaxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): HorizontalScrollView(context, attrs) {

    private val childView: View?
        get() {
            return if (childCount > 0) getChildAt(0) else null
        }

    private var parallaxWidth: Float = 1f
    private var scrollOffset: Float = 0f

    init {
        isHorizontalScrollBarEnabled = false
    }

    fun setParallaxWidth(@FloatRange(from = 1.0) width: Float) {
        if (parallaxWidth != width) {
            parallaxWidth = width
            requestLayout()
        }
    }

    fun setScrollOffset(@FloatRange(from = 0.0, to = 1.0) scrollOffset: Float) {
        this.scrollOffset = scrollOffset
        childView?.doOnLayout { child ->
            val parentWidth = measuredWidth
            val childWidth = child.measuredWidth
            val targetScrollOffsetInPx = ((childWidth - parentWidth) * scrollOffset).toInt()
            if (targetScrollOffsetInPx >= 0) {
                scrollTo(targetScrollOffsetInPx, 0)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val childView = this.childView ?: return
        val parentWidth = this.measuredWidth
        val targetChildWidth = (parentWidth * parallaxWidth).toInt()

        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(targetChildWidth, MeasureSpec.EXACTLY)
        val heightPadding = paddingTop + paddingBottom
        val childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, heightPadding, ViewGroup.LayoutParams.MATCH_PARENT)
        childView.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setScrollOffset(scrollOffset)
    }

}