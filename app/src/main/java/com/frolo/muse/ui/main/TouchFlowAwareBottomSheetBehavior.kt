package com.frolo.muse.ui.main

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.frolo.core.ui.touch.TouchFlowAware
import com.google.android.material.bottomsheet.BottomSheetBehavior


internal class TouchFlowAwareBottomSheetBehavior<V: View>
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ): BottomSheetBehavior<V>(context, attrs), TouchFlowAware {

    override var touchFlowCallback: TouchFlowAware.TouchFlowCallback? = null

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        if (isInitialMotionEvent(parent, child, event)) {
            touchFlowCallback?.onTouchFlowStarted()
        }
        if (isTerminalMotionEvent(parent, child, event)) {
            touchFlowCallback?.onTouchFlowEnded()
        }
        return super.onInterceptTouchEvent(parent, child, event)
    }

    private fun isInitialMotionEvent(
        parent: CoordinatorLayout,
        child: V,
        ev: MotionEvent
    ): Boolean {
        return ev.action == MotionEvent.ACTION_DOWN
                && parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
    }

    private fun isTerminalMotionEvent(
        parent: CoordinatorLayout,
        child: V,
        ev: MotionEvent
    ): Boolean {
        return ev.action == MotionEvent.ACTION_UP
                || ev.action == MotionEvent.ACTION_CANCEL
                && parent.isPointInChildBounds(child, ev.x.toInt(), ev.y.toInt())
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <V: View> from(view: View): TouchFlowAwareBottomSheetBehavior<V> {
            val layoutParams = view.layoutParams as CoordinatorLayout.LayoutParams
            return layoutParams.behavior as TouchFlowAwareBottomSheetBehavior<V>
        }
    }
}