package com.frolo.muse.ui.main.library.search.adapter

import android.view.View
import com.frolo.muse.model.media.Genre
import com.frolo.muse.ui.getNameString
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_genre.view.*


class GenreViewHolder(itemView: View): MediaAdapter.MediaViewHolder(itemView) {
    override val viewOptionsMenu: View? = itemView.view_options_menu

    fun bind(
            item: Genre,
            selected: Boolean,
            selectionChanged: Boolean,
            query: String) {

        with(itemView) {
            val res = resources
            tv_genre_name.text = item.getNameString(res).highlight(query)

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }
}