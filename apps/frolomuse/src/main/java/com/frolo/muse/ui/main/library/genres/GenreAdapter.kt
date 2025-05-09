package com.frolo.muse.ui.main.library.genres

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.music.model.Genre
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller


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
        with(holder) {
            val res = holder.itemView.resources
            tvGenreName.text = item.getNameString(res)

            thumbnailLoader.loadGenreThumbnail(item, imvGenreArt)

            imvCheck.setChecked(selected, selectionChanged)

            (itemView as MediaConstraintLayout).setChecked(selected)
        }
    }

    class GenreViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)
        val tvGenreName: TextView = itemView.findViewById(R.id.tv_genre_name)
        val imvGenreArt: ImageView = itemView.findViewById(R.id.imv_genre_art)
        val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)
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