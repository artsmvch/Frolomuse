package com.frolo.muse.ui.main.player.current

import android.content.Context
import android.graphics.PointF
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt


/**
 * Creates a timed smooth scroller, applying [targetPosition].
 */
fun obtainSmoothScroller(
    context: Context,
    targetPosition: Int,
    timeForScrolling: Long,
    distance: Int
): RecyclerView.SmoothScroller =
    TimedSmoothScroller(context, timeForScrolling, distance)
        .apply {
            this.targetPosition = targetPosition
        }

/**
 * This smooth scroller defines the time for the scrolling for any distance.
 * No matter how much [distance] is, the scrolling will take [timeForScrolling].
 * [distance] is used for proper calculations.
 *
 * In addition, the scrolling is performed the way
 * so after the scrolling the target position is in the center of the list.
 */
private class TimedSmoothScroller constructor(
    context: Context,
    private val timeForScrolling: Long,
    private val distance: Int
): LinearSmoothScroller(context) {

    override fun calculateTimeForDeceleration(dx: Int): Int {
        return calculateTimeForScrolling(dx)
    }

    override fun calculateTimeForScrolling(dx: Int): Int {
        return (timeForScrolling * (dx.toFloat() / distance)).roundToInt()
    }

    override fun calculateDtToFit(viewStart: Int, viewEnd: Int, boxStart: Int, boxEnd: Int, snapPreference: Int): Int {
        return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
    }

}