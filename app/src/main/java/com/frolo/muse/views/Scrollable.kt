package com.frolo.muse.views

import android.content.Context
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import com.frolo.ui.KeyboardUtils
import com.frolo.ui.Screen
import kotlin.math.abs


@Px
private fun getDefaultScrollThresholdToHideKeyboard(context: Context): Int {
    // 20 dp? Good choice
    return Screen.dp(context, 20)
}

fun RecyclerView.hideKeyboardOnScroll(
        @Px threshold: Int = getDefaultScrollThresholdToHideKeyboard(context)) {
    this.hideKeyboardOnScroll(threshold, threshold)
}

fun RecyclerView.hideKeyboardOnScroll(@Px thresholdX: Int, @Px thresholdY: Int) {
    val listener = object : RecyclerView.OnScrollListener() {

        var totalDx: Int = 0
        var totalDy: Int = 0

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                totalDx = 0
                totalDy = 0
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            totalDx += dx
            totalDy += dy
            if (abs(totalDx) >= thresholdX || abs(totalDy) >= thresholdY) {
                KeyboardUtils.hideFrom(this@hideKeyboardOnScroll)
            }
        }

    }

    addOnScrollListener(listener)
}