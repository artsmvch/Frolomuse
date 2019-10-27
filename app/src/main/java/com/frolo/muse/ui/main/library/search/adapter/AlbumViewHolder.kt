package com.frolo.muse.ui.main.library.search.adapter

import android.content.ContentUris
import android.view.View
import com.bumptech.glide.RequestManager
import com.frolo.muse.GlideManager
import com.frolo.muse.R
import com.frolo.muse.model.media.Album
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfTracksString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_album.view.*


class AlbumViewHolder(itemView: View): MediaAdapter.MediaViewHolder(itemView) {
    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    fun bind(
            item: Album,
            selected: Boolean,
            selectionChanged: Boolean,
            requestManager: RequestManager,
            query: String) {

        with(itemView) {
            val res = resources

            val albumId = item.id
            val options = GlideManager.get()
                    .requestOptions(albumId)
                    .placeholder(R.drawable.ic_note_square_placeholder)
                    .error(R.drawable.ic_note_square_placeholder)
            requestManager.load(ContentUris.withAppendedId(GlideManager.albumArtUri(), albumId))
                    .apply(options)
                    .into(imv_album_art)

            tv_album_name.text = item.getNameString(res).highlight(query)
            tv_artist_name.text = item.getArtistString(res)
            tv_number_of_tracks.text = item.getNumberOfTracksString(itemView.resources)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}