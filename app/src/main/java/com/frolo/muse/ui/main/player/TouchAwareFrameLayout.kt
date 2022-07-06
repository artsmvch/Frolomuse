package com.frolo.muse.ui.main.player

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.frolo.muse.ui.main.player.TouchAwareFrameLayout.TouchCallback


/**
 * FrameLayout that notifies [TouchCallback] clients when the layout
 * is touched down and when the recent touch is released.
 * No touches are intercepted, this layout only tracks them.
 */
class TouchAwareFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    interface TouchCallback {
        /**
         * Called when the sequence of motion events started
         */
        fun onTouchStarted()

        /**
         * Called when the sequence of motion events ended
         */
        fun onTouchEnded()
    }

    var touchCallback: TouchCallback? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            touchCallback?.onTouchStarted()
        }
        if (ev.action == MotionEvent.ACTION_UP
            || ev.action == MotionEvent.ACTION_CANCEL) {
            touchCallback?.onTouchEnded()
        }
        return super.onInterceptTouchEvent(ev)
    }

}