package com.frolo.muse.ui.main.player.mini

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout


/**
 * Special container for [MiniPlayerFragment] that allows clients to control all touches for all views within it.
 * If [touchesDisabled] is set to true then the container must intercept all motion events.
 * For example, this may be useful in the case the client wants to disable click handlers for all views in the container.
 * Otherwise, it behaves like a regular FrameLayout.
 */
class MiniPlayerContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    var touchesDisabled: Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (touchesDisabled) return true
        return super.onInterceptTouchEvent(ev)
    }

}