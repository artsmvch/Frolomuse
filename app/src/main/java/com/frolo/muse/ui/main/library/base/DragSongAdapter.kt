package com.frolo.muse.ui.main.library.base

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.base.adapter.ItemTouchHelperAdapter
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_drag_song.view.*


class DragSongAdapter constructor(
        requestManager: RequestManager,
        private val onDragListener: OnDragListener? = null
): SongAdapter<Song>(requestManager), ItemTouchHelperAdapter {

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
        remove(position)
    }

    override fun getItemViewType(position: Int): Int {
        return itemViewType
    }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> {
                super.onCreateBaseViewHolder(parent, viewType)
            }

            VIEW_TYPE_SWAPPABLE -> {
                val view = parent.inflateChild(R.layout.item_drag_song)

                SwappableSongViewHolder(view).apply {
                    val viewToDrag = itemView.findViewById<View>(R.id.view_drag_and_drop)
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

    override fun onBindViewHolder(
            holder: SongViewHolder,
            position: Int,
            item: Song,
            selected: Boolean,
            selectionChanged: Boolean
    ) {

        if (holder is SwappableSongViewHolder) {
            with(holder.itemView) {
                val res = resources
                tv_song_name.text = item.getNameString(res)
                tv_artist_name.text = item.getArtistString(res)
                tv_duration.text = item.getDurationString()

                if (position != playingPosition) {
                    mini_visualizer.visibility = View.GONE
                    mini_visualizer.setAnimating(false)
                } else {
                    mini_visualizer.visibility = View.VISIBLE
                    mini_visualizer.setAnimating(isPlaying)
                }

                imv_check.setChecked(selected, selectionChanged)

                view_play_position_background.visibility =
                    if (position != playingPosition) View.INVISIBLE else View.VISIBLE

                isSelected = selected
            }
        } else {
            super.onBindViewHolder(holder, position, item, selected, selectionChanged)
        }
    }

    class SwappableSongViewHolder(
            itemView: View
    ): SongViewHolder(itemView)
}