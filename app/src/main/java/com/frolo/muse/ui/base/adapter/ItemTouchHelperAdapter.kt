package com.frolo.muse.ui.base.adapter


interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemDismiss(position: Int)
    fun onDragEnded()
}
