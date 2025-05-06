package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.music.model.Playlist
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getDateAddedString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.checkable.CheckView


class PlaylistViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    private val tvPlaylistName: TextView = itemView.findViewById(R.id.tv_playlist_name)
    private val tvPlaylistDateModified: TextView =
        itemView.findViewById(R.id.tv_playlist_date_modified)
    private val imvPlaylistArt: ImageView = itemView.findViewById(R.id.imv_playlist_art)
    private val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)

    fun bind(
            item: Playlist,
            selected: Boolean,
            selectionChanged: Boolean,
            query: String
    ) {

        with(itemView) {
            val res = resources

            tvPlaylistName.text = highlight(text = item.getNameString(resources), part = query)
            tvPlaylistDateModified.text = item.getDateAddedString(res)

            thumbnailLoader.loadPlaylistThumbnail(item, imvPlaylistArt)

            imvCheck.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}