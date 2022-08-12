package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.frolo.muse.R
import com.frolo.music.model.Album
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfTracksString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_album.view.*


class AlbumViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    fun bind(
        item: Album,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            tv_album_name.text = highlight(text = item.getNameString(resources), part = query)
            tv_artist_name.text = item.getArtistString(resources)
            tv_number_of_tracks.text = item.getNumberOfTracksString(resources)

            thumbnailLoader.loadAlbumThumbnail(item, imv_album_art)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}