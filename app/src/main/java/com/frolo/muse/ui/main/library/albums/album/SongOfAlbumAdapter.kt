package com.frolo.muse.ui.main.library.albums.album

import android.view.View
import android.view.ViewGroup
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.media.MediaConstraintLayout
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_song_of_album.view.*


class SongOfAlbumAdapter : SongAdapter<Song>() {

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
        with((holder.itemView as MediaConstraintLayout)) {
            val res = resources
            tv_song_number.text = (position + 1).toString()
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.getDurationString()

            val isPlayPosition = position == playingPosition

            if (isPlayPosition) {
                mini_visualizer.visibility = View.VISIBLE
                mini_visualizer.setAnimate(isPlaying)
            } else {
                mini_visualizer.visibility = View.GONE
                mini_visualizer.setAnimate(false)
            }

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
            setPlaying(isPlayPosition)
        }
    }

}