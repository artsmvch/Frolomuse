package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.item_select_song.view.*


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
        val selectorViewHolder = holder as SongSelectorViewHolder
        with(selectorViewHolder.itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.getDurationString()

            chb_select_song.setChecked(checked = selected, animate = selectionChanged)

            isSelected = selected
        }
    }

    class SongSelectorViewHolder(itemView: View): SongViewHolder(itemView) {
        override val viewOptionsMenu: View? = null

        init {
            val drawable =
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_framed_music_note)
            itemView.chb_select_song.setImageDrawable(drawable)
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