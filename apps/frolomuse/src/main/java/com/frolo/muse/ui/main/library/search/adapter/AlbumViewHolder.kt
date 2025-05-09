package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.music.model.Album
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfTracksString
import com.frolo.muse.views.checkable.CheckView


class AlbumViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    private val tvAlbumName: TextView = itemView.findViewById(R.id.tv_album_name)
    private val tvArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)
    private val tvNumberOfTracks: TextView = itemView.findViewById(R.id.tv_number_of_tracks)
    private val imvAlbumArt: ImageView = itemView.findViewById(R.id.imv_album_art)
    private val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)


    fun bind(
        item: Album,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            tvAlbumName.text = highlight(text = item.getNameString(resources), part = query)
            tvArtistName.text = item.getArtistString(resources)
            tvNumberOfTracks.text = item.getNumberOfTracksString(resources)

            thumbnailLoader.loadAlbumThumbnail(item, imvAlbumArt)

            imvCheck.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}