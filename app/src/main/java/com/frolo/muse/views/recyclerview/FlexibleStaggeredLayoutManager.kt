package com.frolo.muse.views.recyclerview

import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager


internal class FlexibleStaggeredLayoutManager(
    orientation: Int,
    val minSpanCount: Int = SPAN_COUNT_NOT_SET,
    @Px val preferredItemSize: Int = ITEM_SIZE_NOT_SET
) : StaggeredGridLayoutManager(orientation, 1) {

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        val parentSizeSpec = if (orientation == RecyclerView.VERTICAL) widthSpec else heightSpec
        spanCount = FlexibleLayoutManagerHelper.calculateSpanCount(
            View.MeasureSpec.getSize(parentSizeSpec), preferredItemSize, minSpanCount)
    }

    companion object {
        const val SPAN_COUNT_NOT_SET = FlexibleLayoutManagerHelper.SPAN_COUNT_NOT_SET
        const val ITEM_SIZE_NOT_SET = FlexibleLayoutManagerHelper.ITEM_SIZE_NOT_SET
    }
}