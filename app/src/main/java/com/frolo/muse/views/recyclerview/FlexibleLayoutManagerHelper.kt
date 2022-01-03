package com.frolo.muse.views.recyclerview

import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager


internal object FlexibleLayoutManagerHelper {
    const val SPAN_COUNT_NOT_SET = -1
    const val ITEM_SIZE_NOT_SET = -1

    fun calculateSpanCount(parentSize: Int, @Px preferredItemSize: Int, minSpanCount: Int): Int {
        val spanCount = if (preferredItemSize > 0) {
            parentSize / preferredItemSize
        } else {
            return GridLayoutManager.DEFAULT_SPAN_COUNT
        }

        return if (minSpanCount > 0) {
            spanCount.coerceAtLeast(minSpanCount)
        } else {
            spanCount
        }
    }
}