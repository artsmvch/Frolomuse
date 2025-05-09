package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.checkable.CheckableImageView
import com.frolo.music.model.Song


class SongSelectorAdapter(thumbnailLoader: ThumbnailLoader):
    SongAdapter<Song>(thumbnailLoader, SongItemCallback()) {

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int) =
            SongSelectorViewHolder(parent.inflateChild(R.layout.item_select_song))

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: Song,
        selected: Boolean, selectionChanged: Boolean
    ) {
        super.onBindViewHolder(holder, position, item, selected, selectionChanged)
        holder as SongSelectorViewHolder
        holder.chbSelectSong.setChecked(checked = selected, animate = selectionChanged)
    }

    class SongSelectorViewHolder(itemView: View): SongViewHolder(itemView) {
        override val viewOptionsMenu: View? = null

        val chbSelectSong: CheckableImageView = itemView.findViewById(R.id.chb_select_song)

        init {
            chbSelectSong.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_framed_music_note)
            )
        }
    }

    private class SongItemCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.duration == newItem.duration &&
                oldItem.title == newItem.title &&
                oldItem.artist == newItem.artist
        }
    }

}