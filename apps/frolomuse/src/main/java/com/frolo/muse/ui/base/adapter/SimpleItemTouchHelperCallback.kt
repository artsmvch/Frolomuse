package com.frolo.muse.ui.base.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


class SimpleItemTouchHelperCallback constructor(
        private val adapter: ItemTouchHelperAdapter,
        private val longPressDragEnabled: Boolean = false,
        private val itemViewSwipeEnabled: Boolean = true
) : ItemTouchHelper.Callback() {

    private var startPosition: Int? = null
    private var resultPosition: Int? = null

    override fun isLongPressDragEnabled() = longPressDragEnabled

    override fun isItemViewSwipeEnabled() = itemViewSwipeEnabled

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (startPosition == null) {
            // This is the first move
            startPosition = viewHolder.adapterPosition
        }
        resultPosition = target.adapterPosition

        adapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        startPosition?.also { safeStartPosition ->
            resultPosition?.also { resultPosition ->
                if (safeStartPosition != resultPosition) {
                    adapter.onDragEndedWithResult(safeStartPosition, resultPosition)
                } else {
                    adapter.onDragEnded()
                }
            }
        }

        startPosition = null
        resultPosition = null
    }
}
