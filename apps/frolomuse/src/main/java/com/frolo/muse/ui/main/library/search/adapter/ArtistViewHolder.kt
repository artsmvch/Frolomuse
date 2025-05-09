package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.music.model.Artist
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfAlbumsString
import com.frolo.muse.ui.getNumberOfTracksString
import com.frolo.muse.views.checkable.CheckView


class ArtistViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    private val tvArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)
    private val tvNumberOfAlbums: TextView = itemView.findViewById(R.id.tv_number_of_albums)
    private val tvNumberOfTracks: TextView = itemView.findViewById(R.id.tv_number_of_tracks)
    private val imvArtistArt: ImageView = itemView.findViewById(R.id.imv_artist_art)
    private val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)

    fun bind(
        item: Artist,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            val res = resources

            tvArtistName.text = highlight(text = item.getNameString(resources), part = query)
            tvNumberOfAlbums.text = item.getNumberOfAlbumsString(res)
            tvNumberOfTracks.text = item.getNumberOfTracksString(res)

            thumbnailLoader.loadArtistThumbnail(item, imvArtistArt)

            imvCheck.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}