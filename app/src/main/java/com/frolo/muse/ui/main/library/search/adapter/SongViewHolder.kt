package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_song.view.*


class SongViewHolder(itemView: View): MediaAdapter.MediaViewHolder(itemView) {
    override val viewOptionsMenu: View? = itemView.view_options_menu
    init {
        itemView.mini_visualizer.visibility = View.GONE
    }

    fun bind(
        item: Song,
        selected: Boolean,
        selectionChanged: Boolean,
        requestManager: RequestManager,
        query: String
    ) {

        with(itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res).highlight(query)
            tv_artist_name.text = item.getAlbumString(res)
            tv_duration.text = item.getDurationString()

            requestManager.makeRequest(item.albumId)
                    .placeholder(R.drawable.ic_framed_music_note)
                    .error(R.drawable.ic_framed_music_note)
                    .circleCrop()
                    .into(imv_album_art)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}