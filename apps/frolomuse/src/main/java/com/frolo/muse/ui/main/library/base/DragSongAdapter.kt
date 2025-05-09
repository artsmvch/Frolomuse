package com.frolo.muse.ui.main.library.base

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.adapter.ItemTouchHelperAdapter
import com.frolo.music.model.Song


open class DragSongAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader,
    private val onDragListener: OnDragListener? = null
): SongAdapter<Song>(thumbnailLoader), ItemTouchHelperAdapter {

    companion object {
        const val VIEW_TYPE_NORMAL = 0
        const val VIEW_TYPE_SWAPPABLE = 1
    }

    interface OnDragListener {
        fun onTouchDragView(holder: RecyclerView.ViewHolder)
        fun onItemDismissed(position: Int)
        fun onItemMoved(fromPosition: Int, toPosition: Int)
    }

    var itemViewType: Int = VIEW_TYPE_SWAPPABLE
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        moveItem(fromPosition, toPosition)
    }

    override fun onDragEndedWithResult(fromPosition: Int, toPosition: Int) {
        onDragListener?.onItemMoved(fromPosition, toPosition)
    }

    override fun onDragEnded() = Unit

    override fun onItemDismiss(position: Int) {
        onDragListener?.onItemDismissed(position)
        removeItemAt(position)
    }

    override fun getItemViewType(position: Int): Int {
        return itemViewType
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> {
                super.onCreateBaseViewHolder(parent, viewType)
            }

            VIEW_TYPE_SWAPPABLE -> {
                val view = parent.inflateChild(R.layout.item_drag_song)
                SongViewHolder(view).apply {
                    val viewToDrag = itemView.findViewById<View>(R.id.icon)
                    viewToDrag.setOnTouchListener { _, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                onDragListener?.onTouchDragView(this@apply)
                                false
                            }

                            else -> false
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Unexpected view type: $viewType")
        }
    }

}