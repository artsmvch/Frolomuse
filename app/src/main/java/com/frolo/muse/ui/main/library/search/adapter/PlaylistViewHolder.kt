package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.getDateAddedString
import com.frolo.muse.ui.getNameString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_playlist.view.*


class PlaylistViewHolder(itemView: View): MediaAdapter.MediaViewHolder(itemView) {
    override val viewOptionsMenu: View? = itemView.view_options_menu

    fun bind(
            item: Playlist,
            selected: Boolean,
            selectionChanged: Boolean,
            query: String) {

        with(itemView) {
            val res = resources

            tv_playlist_name.text = highlight(text = item.getNameString(resources), part = query)
            tv_playlist_date_modified.text = item.getDateAddedString(res)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}