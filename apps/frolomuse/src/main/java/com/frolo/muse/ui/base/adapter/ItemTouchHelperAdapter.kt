package com.frolo.muse.ui.base.adapter


/**
 * Delegates drag&drop and swipe events of items in [androidx.recyclerview.widget.RecyclerView].
 */
interface ItemTouchHelperAdapter {
    /**
     * Called when an item gets moved from [fromPosition] to [toPosition].
     * The current drag is still happening.
     */
    fun onItemMove(fromPosition: Int, toPosition: Int)

    /**
     * Called when the current drag is ended.
     * The result of this drag is the move from [fromPosition] to [toPosition].
     * Any additional cleanups should go here.
     * NOTE: this method will not be called if [onDragEnded] was called.
     */
    fun onDragEndedWithResult(fromPosition: Int, toPosition: Int)

    /**
     * Called when the current drag is ended,
     * but the dragged item ultimately stayed at the same position.
     * Any additional cleanups should go here.
     * NOTE: this method will not be called if [onDragEndedWithResult] was called.
     */
    fun onDragEnded()

    /**
     * Called when an item gets dismissed with a swipe.
     */
    fun onItemDismiss(position: Int)
}
