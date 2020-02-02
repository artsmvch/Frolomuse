package com.frolo.muse.ui.main.library.artists.artist.songs

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_song_of_artist.view.*


class SongOfArtistAdapter constructor(
        private val requestManager: RequestManager
): SongAdapter<Song>(requestManager) {

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

        with(holder.itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_album_name.text = item.getAlbumString(res)
            tv_duration.text = item.getDurationString()

            val isPlayPosition = position == playingPosition

            if (isPlayPosition) {
                mini_visualizer.visibility = View.VISIBLE
                mini_visualizer.setAnimating(isPlaying)
            } else {
                mini_visualizer.visibility = View.GONE
                mini_visualizer.setAnimating(false)
            }

            view_play_position_background.visibility =
                    if (isPlayPosition) View.VISIBLE else View.INVISIBLE

            requestManager.makeRequest(item.albumId)
                    .placeholder(R.drawable.ic_note_rounded_placeholder)
                    .error(R.drawable.ic_note_rounded_placeholder)
                    .circleCrop()
                    .into(imv_album_art)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }

}