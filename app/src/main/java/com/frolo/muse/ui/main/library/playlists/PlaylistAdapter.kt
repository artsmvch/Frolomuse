package com.frolo.muse.ui.main.library.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.ui.getDateAddedString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.util.CharSequences
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_playlist.view.*


class PlaylistAdapter : BaseAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistItemCallback),
        FastScroller.SectionIndexer {

    override fun onCreateBaseViewHolder(
            parent: ViewGroup,
            viewType: Int): PlaylistViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun getSectionText(position: Int): CharSequence {
        if (position >= itemCount) return CharSequences.empty()
        return getItemAt(position).name.let { name ->
            CharSequences.firstCharOrEmpty(name)
        }
    }

    override fun onBindViewHolder(
            holder: PlaylistViewHolder,
            position: Int,
            item: Playlist,
            selected: Boolean, selectionChanged: Boolean) {

        with(holder.itemView) {
            tv_playlist_name.text = item.getNameString(resources)
            tv_playlist_date_modified.text = item.getDateAddedString(resources)

            imv_check.setChecked(selected, selectionChanged)
            
            isSelected = selected
        }
    }

    override fun getItemId(position: Int) = getItemAt(position).id

    class PlaylistViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.view_options_menu
    }

    object PlaylistItemCallback: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

    }
}
