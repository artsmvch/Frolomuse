package com.frolo.muse.ui.main.library.albums

import android.content.Context
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


internal class AlbumGridLayoutManager(
    context: Context,
    val minSpanCount: Int = SPAN_COUNT_NOT_SET,
    @Px val preferredItemWidth: Int = ITEM_WITH_NOT_SET
) : GridLayoutManager(context, DEFAULT_SPAN_COUNT) {

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        spanCount = calculateSpanCount(View.MeasureSpec.getSize(widthSpec))
    }

    private fun calculateSpanCount(parentWidth: Int): Int {
        val spanCount = if (preferredItemWidth > 0) {
            parentWidth / preferredItemWidth
        } else {
            return DEFAULT_SPAN_COUNT
        }

        return if (minSpanCount > 0) {
            spanCount.coerceAtLeast(minSpanCount)
        } else {
            spanCount
        }
    }

    companion object {
        private const val DEFAULT_SPAN_COUNT = 1

        const val SPAN_COUNT_NOT_SET = -1
        const val ITEM_WITH_NOT_SET = -1
    }
}