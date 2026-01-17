package com.frolo.muse.ui.main.library.playlists

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getDateAddedString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.frolo.music.model.Playlist
import com.l4digital.fastscroll.FastScroller


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

        with(holder) {
            tvPlaylistName.text = item.getNameString(itemView.resources)
            tvPlaylistDateModified.text = item.getDateAddedString(itemView.resources)

            thumbnailLoader.loadPlaylistThumbnail(item, imvPlaylistArt)

            imvCheck.setChecked(selected, selectionChanged)

            (itemView as MediaConstraintLayout).setChecked(selected)
        }
    }

    override fun getItemId(position: Int) = getItemAt(position).getMediaId().getSourceId()

    class PlaylistViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)
        val tvPlaylistName: TextView = itemView.findViewById(R.id.tv_playlist_name)
        val tvPlaylistDateModified: TextView = itemView.findViewById(R.id.tv_playlist_date_modified)
        val imvPlaylistArt: ImageView = itemView.findViewById(R.id.imv_playlist_art)
        val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)
    }

    object PlaylistItemCallback: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.isFromSharedStorage == newItem.isFromSharedStorage && oldItem.getMediaId().getSourceId() == newItem.getMediaId().getSourceId()
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

    }
}
