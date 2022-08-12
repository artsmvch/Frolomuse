package com.frolo.muse.ui.main.library.artists

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.music.model.Artist
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfAlbumsString
import com.frolo.muse.ui.getNumberOfTracksString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_artist.view.*


class ArtistAdapter(
    private val thumbnailLoader: ThumbnailLoader
): BaseAdapter<Artist, ArtistAdapter.ArtistViewHolder>(ArtistItemCallback),
        FastScroller.SectionIndexer {

    override fun getSectionText(position: Int) = sectionIndexAt(position) { name }

    override fun getItemId(position: Int) = getItemAt(position).id

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ArtistViewHolder(parent.inflateChild(R.layout.item_artist))

    override fun onBindViewHolder(
        holder: ArtistViewHolder,
        position: Int,
        item: Artist,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        with(holder.itemView as MediaConstraintLayout) {
            val res = holder.itemView.resources

            tv_artist_name.text = item.getNameString(res)
            tv_number_of_albums.text = item.getNumberOfAlbumsString(res)
            tv_number_of_tracks.text = item.getNumberOfTracksString(res)

            thumbnailLoader.loadArtistThumbnail(item, imv_artist_art)

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
        }
    }

    class ArtistViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View = itemView.findViewById(R.id.view_options_menu)
    }

    object ArtistItemCallback: DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }

    }
}