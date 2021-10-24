package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.glide.makeRequest
import com.frolo.muse.model.media.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_song.view.*


class SongViewHolder constructor(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.view_options_menu

    init {
        itemView.mini_visualizer.visibility = View.GONE
    }

    fun bind(
        item: Song,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            val res = resources
            tv_song_name.text = highlight(text = item.getNameString(resources), part = query)
            tv_artist_name.text = item.getAlbumString(res)
            tv_duration.text = item.getDurationString()

            thumbnailLoader.loadSongThumbnail(item, imv_song_thumbnail)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}