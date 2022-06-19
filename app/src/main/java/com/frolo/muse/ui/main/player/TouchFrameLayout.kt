package com.frolo.muse.ui.main.player

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout


/**
 * FrameLayout that notifies [TouchCallback] clients when the layout is touched down and when the recent touch is released.
 * No touches are intercepted, this layout only tracks them.
 */
class TouchFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): CoordinatorLayout(context, attrs, defStyleAttr) {

    interface TouchCallback {
        fun onTouchDown()
        fun onTouchUp()
    }

    var touchCallback: TouchCallback? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            touchCallback?.onTouchDown()
        }
        if (ev.action == MotionEvent.ACTION_UP && ev.action == MotionEvent.ACTION_CANCEL) {
            touchCallback?.onTouchUp()
        }
        return super.onInterceptTouchEvent(ev)
    }

}