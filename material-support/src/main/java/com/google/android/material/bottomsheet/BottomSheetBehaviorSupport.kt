package com.google.android.material.bottomsheet

import android.view.View
import androidx.core.view.doOnPreDraw


object BottomSheetBehaviorSupport {

    fun dispatchOnSlide(bottomSheet: View) {
        // Use onPreDraw, not onLayout!
        bottomSheet.doOnPreDraw { view ->
            val behaviour = BottomSheetBehavior.from(view)
            dispatchOnSlideImmediately(behaviour)
        }
    }

    private fun dispatchOnSlideImmediately(behavior: BottomSheetBehavior<*>) {
        val bottomSheet: View = behavior.viewRef?.get() ?: return
        behavior.dispatchOnSlide(bottomSheet.top)
    }

}