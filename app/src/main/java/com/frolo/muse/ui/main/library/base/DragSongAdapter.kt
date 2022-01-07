package com.frolo.muse.ui.main.library.base

import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.ui.StyleUtils
import com.frolo.muse.dp2px
import com.frolo.muse.inflateChild
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.adapter.ItemTouchHelperAdapter
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.media.MediaConstraintLayout
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_drag_song.view.*


class DragSongAdapter constructor(
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

                DragSongViewHolder(view).apply {
                    val viewToDrag = itemView.findViewById<View>(R.id.include_song_art_container)
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

        if (holder is DragSongViewHolder) {
            val isPlayPosition = position == playPosition

            with(holder.itemView as MediaConstraintLayout) {
                val res = resources
                tv_song_name.text = item.getNameString(res)
                tv_artist_name.text = item.getArtistString(res)
                tv_duration.text = item.getDurationString()

                imv_check.setChecked(selected, selectionChanged)

                setChecked(selected)
                setPlaying(isPlayPosition)
            }

            holder.resolvePlayingPosition(
                isPlayPosition = isPlayPosition,
                isPlaying = isPlaying
            )
        } else {
            super.onBindViewHolder(holder, position, item, selected, selectionChanged)
        }
    }

    class DragSongViewHolder(itemView: View): SongViewHolder(itemView) {

        private val dragIconSize: Int = 16f.dp2px(itemView.context).toInt()

        init {
            itemView.imv_song_thumbnail.apply {
                updateLayoutParams {
                    width = dragIconSize
                    height = dragIconSize
                }
                setImageResource(R.drawable.ic_drag_burger)
                imageTintList =
                    ColorStateList.valueOf(StyleUtils.resolveColor(context, R.attr.iconImageTint))
            }
        }

    }

}