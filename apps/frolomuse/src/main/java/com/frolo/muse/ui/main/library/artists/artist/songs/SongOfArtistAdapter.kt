package com.frolo.muse.ui.main.library.artists.artist.songs

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.music.model.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.media.MediaConstraintLayout
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_song_of_artist.view.*


class SongOfArtistAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader,
): SongAdapter<Song>(thumbnailLoader, SongItemCallback()) {

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SongViewHolder(parent.inflateChild(R.layout.item_song_of_artist))

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: Song,
        selected: Boolean, selectionChanged: Boolean
    ) {

        val isPlayPosition = position == playPosition

        with(holder.itemView as MediaConstraintLayout) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_album_name.text = item.getAlbumString(res)
            tv_duration.text = item.getDurationString()

            thumbnailLoader.loadSongThumbnail(item, imv_song_thumbnail)

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
            setPlaying(isPlayPosition)
        }

        holder.resolvePlayingPosition(
            isPlayPosition = isPlayPosition,
            isPlaying = isPlaying
        )
    }

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