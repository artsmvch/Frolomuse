package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.frolo.muse.model.media.Artist
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfAlbumsString
import com.frolo.muse.ui.getNumberOfTracksString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_artist.view.*


class ArtistViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.view_options_menu

    fun bind(
        item: Artist,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            val res = resources

            tv_artist_name.text = highlight(text = item.getNameString(resources), part = query)
            tv_number_of_albums.text = item.getNumberOfAlbumsString(res)
            tv_number_of_tracks.text = item.getNumberOfTracksString(res)

            thumbnailLoader.loadArtistThumbnail(item, imv_artist_art)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}