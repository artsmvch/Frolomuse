package com.frolo.muse.views.decorations

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.frolo.debug.DebugUtils
import java.lang.IllegalStateException


class UniformItemDecoration(@Px private val offset: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager ?: return
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (layoutManager is StaggeredGridLayoutManager) {
//            val layoutParams = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
//            val isFullSpan = layoutParams.isFullSpan
//            val spanIndex = layoutParams.spanIndex
            val spanCount = layoutManager.spanCount
            val isFirstRow: Boolean
            val isFirstColumn: Boolean
            val isLastRow: Boolean
            val isLastColumn: Boolean
            when (layoutManager.orientation) {
                RecyclerView.VERTICAL -> {
                    isFirstRow = itemPosition < spanCount
                    isFirstColumn = itemPosition % spanCount == 0
                    isLastRow = itemPosition >= itemCount - spanCount
                    isLastColumn = itemPosition % spanCount == spanCount - 1
                }
                RecyclerView.HORIZONTAL -> {
                    isFirstRow = itemPosition % spanCount == 0
                    isFirstColumn = itemPosition < spanCount
                    isLastRow = itemPosition % spanCount == spanCount - 1
                    isLastColumn = itemPosition >= itemCount - spanCount
                }
                else -> {
                    // illegal
                    isFirstRow = false
                    isFirstColumn = false
                    isLastRow = false
                    isLastColumn = false
                }
            }

            outRect.left = if (isFirstColumn) offset else offset / 2
            outRect.top = if (isFirstRow) offset else offset / 2
            outRect.right = if (isLastColumn) offset else offset / 2
            outRect.bottom = if (isLastRow) offset else offset / 2
        } else {
            val errorMessage = "Unexpected layout manager: $layoutManager"
            DebugUtils.dumpOnMainThread(IllegalStateException(errorMessage))
        }
    }

}