package com.frolo.muse.ui.main.library.albums.album

import android.view.ViewGroup
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getTrackNumberString
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.media.MediaConstraintLayout
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_song_of_album.view.*


class SongOfAlbumAdapter(thumbnailLoader: ThumbnailLoader) : SongAdapter<Song>(thumbnailLoader) {

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SongViewHolder(parent.inflateChild(R.layout.item_song_of_album))

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: Song,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        val isPlayPosition = position == playPosition

        with((holder.itemView as MediaConstraintLayout)) {
            val res = resources
            tv_song_number.text = item.getTrackNumberString(res)
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

    }

}