package com.frolo.muse.ui.main.library.playlists

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getDateAddedString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_playlist.view.*


class PlaylistAdapter(
    private val thumbnailLoader: ThumbnailLoader
) : BaseAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistItemCallback),
        FastScroller.SectionIndexer {

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = PlaylistViewHolder(parent.inflateChild(R.layout.item_playlist))

    override fun getSectionText(position: Int) = sectionIndexAt(position) { name }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int,
        item: Playlist,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        with(holder.itemView as MediaConstraintLayout) {
            tv_playlist_name.text = item.getNameString(resources)
            tv_playlist_date_modified.text = item.getDateAddedString(resources)

            thumbnailLoader.loadPlaylistThumbnail(item, imv_playlist_art)

            imv_check.setChecked(selected, selectionChanged)
            
            setChecked(selected)
        }
    }

    override fun getItemId(position: Int) = getItemAt(position).id

    class PlaylistViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.view_options_menu
    }

    object PlaylistItemCallback: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.isFromSharedStorage == newItem.isFromSharedStorage && oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

    }
}
