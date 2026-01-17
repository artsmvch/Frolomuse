package com.frolo.muse.ui.main.library.artists

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getNumberOfAlbumsString
import com.frolo.muse.ui.getNumberOfTracksString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.frolo.music.model.Artist
import com.l4digital.fastscroll.FastScroller


class ArtistAdapter(
    private val thumbnailLoader: ThumbnailLoader
): BaseAdapter<Artist, ArtistAdapter.ArtistViewHolder>(ArtistItemCallback),
        FastScroller.SectionIndexer {

    override fun getSectionText(position: Int) = sectionIndexAt(position) { name }

    override fun getItemId(position: Int) = getItemAt(position).getMediaId().getSourceId()

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
        with(holder) {
            val res = itemView.resources
            tvArtistName.text = item.getNameString(res)
            tvNumberOfAlbums.text = item.getNumberOfAlbumsString(res)
            tvNumberOfTracks.text = item.getNumberOfTracksString(res)
            thumbnailLoader.loadArtistThumbnail(item, imvArtistArt)
            imvCheck.setChecked(selected, selectionChanged)
            (itemView as MediaConstraintLayout).setChecked(selected)
        }
    }

    class ArtistViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View = itemView.findViewById(R.id.view_options_menu)

        val tvArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)
        val tvNumberOfAlbums: TextView = itemView.findViewById(R.id.tv_number_of_albums)
        val tvNumberOfTracks: TextView = itemView.findViewById(R.id.tv_number_of_tracks)
        val imvArtistArt: ImageView = itemView.findViewById(R.id.imv_artist_art)
        val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)
    }

    object ArtistItemCallback: DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.getMediaId().getSourceId() == newItem.getMediaId().getSourceId()
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }

    }
}