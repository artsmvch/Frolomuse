package com.frolo.muse.ui.main.library.artists.artist.songs

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.music.model.Song


class SongOfArtistAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader,
): SongAdapter<Song>(thumbnailLoader, SongItemCallback()) {

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SongViewHolder(parent.inflateChild(R.layout.item_song_of_artist))

    private class SongItemCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.duration == newItem.duration &&
                oldItem.albumId == newItem.albumId &&
                oldItem.title == newItem.title &&
                oldItem.album == newItem.album
        }
    }

}