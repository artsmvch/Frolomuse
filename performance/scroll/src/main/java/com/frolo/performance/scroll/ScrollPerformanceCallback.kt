package com.frolo.performance.scroll

import androidx.recyclerview.widget.RecyclerView


fun interface ScrollPerformanceCallback {
    /**
     * Called on poor scroll performance of [listView]. [info] contains detailed information
     * about the scroll performance.
     * This method should be lightweight as it affects the scrolling itself.
     * NOTE: do not store the [info] object as it may change. Instead, store it properties.
     */
    fun onPoorScrollPerformance(listView: RecyclerView, info: ScrollPerformanceInfo)
}