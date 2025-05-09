package com.frolo.muse.ui.main.library.mostplayed

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getLastTimePlayedString
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.music.model.SongWithPlayCount


class SongWithPlayCountAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader,
): SongAdapter<SongWithPlayCount>(thumbnailLoader, SongWithPlayCountItemCallback()) {

    private companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_WITH_LAST_PLAY_TIME = 1
    }

    override fun getItemViewType(position: Int): Int {
        return getItemAt(position).let { item ->
            if (item.hasLastPlayTime()) VIEW_TYPE_WITH_LAST_PLAY_TIME
            else VIEW_TYPE_DEFAULT
        }
    }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int) =
        SongWithPlayCountViewHolder(
            itemView = parent.inflateChild(R.layout.item_song_with_play_count),
            hasLastPlayTime = viewType == VIEW_TYPE_WITH_LAST_PLAY_TIME
        )

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: SongWithPlayCount,
        selected: Boolean,
        selectionChanged: Boolean
    ) {
        super.onBindViewHolder(holder, position, item, selected, selectionChanged)

        holder as SongWithPlayCountViewHolder
        val ctx = holder.itemView.context
        val res = holder.itemView.resources
        if (item.hasLastPlayTime()) {
            holder.tvPlayCount.text = res.getQuantityString(R.plurals.played_s_times, item.playCount, item.playCount)
            holder.tvLastTimePlayed.text = res.getString(R.string.last_time_s, item.getLastTimePlayedString(ctx))
        } else {
            holder.tvPlayCount.text = res.getString(R.string.not_played_yet)
            holder.tvLastTimePlayed.text = null
        }
    }

    class SongWithPlayCountViewHolder(
        itemView: View,
        hasLastPlayTime: Boolean
    ): SongViewHolder(itemView) {

        val tvLastTimePlayed: TextView = itemView.findViewById(R.id.tv_last_time_played)
        val tvPlayCount: TextView = itemView.findViewById(R.id.tv_play_count)

        init {
            tvLastTimePlayed.visibility = if (hasLastPlayTime) View.VISIBLE else View.GONE
        }

        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)
    }

    private class SongWithPlayCountItemCallback : DiffUtil.ItemCallback<SongWithPlayCount>() {
        override fun areItemsTheSame(oldItem: SongWithPlayCount, newItem: SongWithPlayCount): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SongWithPlayCount, newItem: SongWithPlayCount): Boolean {
            return oldItem.duration == newItem.duration &&
                oldItem.albumId == newItem.albumId &&
                oldItem.title == newItem.title &&
                oldItem.artist == newItem.artist &&
                oldItem.playCount == newItem.playCount &&
                oldItem.lastPlayTime == newItem.lastPlayTime
        }
    }

}