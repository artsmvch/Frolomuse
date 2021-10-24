package com.frolo.muse.ui.main.library.genres

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Genre
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_genre.view.*


class GenreAdapter(
    private val thumbnailLoader: ThumbnailLoader
): BaseAdapter<Genre, GenreAdapter.GenreViewHolder>(GenreItemCallback),
        FastScroller.SectionIndexer {

    override fun getItemId(position: Int) = getItemAt(position).id

    override fun getSectionText(position: Int) = sectionIndexAt(position) { name }

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = GenreViewHolder(parent.inflateChild(R.layout.item_genre))

    override fun onBindViewHolder(
        holder: GenreViewHolder,
        position: Int,
        item: Genre,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        with(holder.itemView as MediaConstraintLayout) {
            val res = holder.itemView.resources
            tv_genre_name.text = item.getNameString(res)

            thumbnailLoader.loadGenreThumbnail(item, imv_genre_art)

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
        }
    }

    class GenreViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)
    }

    object GenreItemCallback: DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem == newItem
        }

    }
}