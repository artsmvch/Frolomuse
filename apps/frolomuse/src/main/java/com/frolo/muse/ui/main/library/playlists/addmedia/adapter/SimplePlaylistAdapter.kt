package com.frolo.muse.ui.main.library.playlists.addmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.frolo.muse.R
import com.frolo.music.model.Playlist
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import kotlinx.android.synthetic.main.item_simple_playlist.view.*


class SimplePlaylistAdapter:
        BaseAdapter<Playlist, SimplePlaylistAdapter.SimplePlaylistViewHolder>() {

    override fun onCreateBaseViewHolder(
            parent: ViewGroup,
            viewType: Int): SimplePlaylistViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_simple_playlist, parent, false)
        return SimplePlaylistViewHolder(view)
    }

    override fun onBindViewHolder(
            holder: SimplePlaylistViewHolder,
            position: Int,
            item: Playlist,
            selected: Boolean,
            selectionChanged: Boolean) {

        with(holder.itemView) {
            tv_playlist_name.text = item.getNameString(resources)
        }
    }

    class SimplePlaylistViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = null
    }
}