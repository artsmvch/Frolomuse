package com.frolo.muse.ui.main.player.current

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs


/**
 * Observers the vertical scrolling.
 * If scrolled more than [threshold] pixels from the starting position when this method was called,
 * then [onScroll] callback is invoked and the observation ends.
 */
fun RecyclerView.doOnVerticalScroll(threshold: Int = 0, onScroll: () -> Unit) {
    val listener = object : RecyclerView.OnScrollListener() {
        var currThreshold: Int = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            currThreshold += dy
            if (abs(currThreshold) >= threshold) {
                removeOnScrollListener(this)
                onScroll.invoke()
            }
        }
    }
    addOnScrollListener(listener)
}
