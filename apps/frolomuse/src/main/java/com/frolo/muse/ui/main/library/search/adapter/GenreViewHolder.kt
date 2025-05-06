package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.music.model.Genre
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.checkable.CheckView


class GenreViewHolder(
    private val itemView: View,
    private val thumbnailLoader: ThumbnailLoader
): MediaAdapter.MediaViewHolder(itemView) {

    override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

    private val tvGenreName: TextView = itemView.findViewById(R.id.tv_genre_name)
    private val imvGenreArt: ImageView = itemView.findViewById(R.id.imv_genre_art)
    private val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)


    fun bind(
        item: Genre,
        selected: Boolean,
        selectionChanged: Boolean,
        query: String
    ) {

        with(itemView) {
            tvGenreName.text = highlight(text = item.getNameString(resources), part = query)

            thumbnailLoader.loadGenreThumbnail(item, imvGenreArt)

            imvCheck.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}