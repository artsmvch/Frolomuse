package com.frolo.muse.ui.main.library.playlists.addmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.frolo.muse.R
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.music.model.Playlist
import kotlinx.android.synthetic.main.item_playlist_selector.view.*


class PlaylistSelectorAdapter:
        BaseAdapter<Playlist, PlaylistSelectorAdapter.SimplePlaylistViewHolder>() {

    private val onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        val playlist = view.tag as? Playlist
        if (playlist != null) {
            if (isChecked) {
                _checkedPlaylists[playlist.identifier] = playlist
            } else {
                _checkedPlaylists.remove(playlist.identifier)
            }
            onCheckedPlaylistsChangeListener?.onCheckedPlaylistsChanged(_checkedPlaylists)
        }
    }

    private val _checkedPlaylists = HashMap<Playlist.Identifier, Playlist>()

    var onCheckedPlaylistsChangeListener: OnCheckedPlaylistsChangeListener? = null

    init {
        listener = object : BaseAdapter.Listener<Playlist> {
            override fun onItemClick(item: Playlist, position: Int) {
                findViewByPosition(position)?.checkbox?.also { checkbox ->
                    checkbox.toggle()
                }
            }
        }
    }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): SimplePlaylistViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_selector, parent, false)
        return SimplePlaylistViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: SimplePlaylistViewHolder,
        position: Int,
        item: Playlist,
        selected: Boolean,
        selectionChanged: Boolean
    ) = with(holder.itemView) {
        checkbox.tag = item
        checkbox.setOnCheckedChangeListener(null)
        checkbox.isChecked = _checkedPlaylists.contains(item.identifier)
        checkbox.setOnCheckedChangeListener(onCheckedChangeListener)
        title.text = item.getNameString(resources)
    }

    fun interface OnCheckedPlaylistsChangeListener {
        fun onCheckedPlaylistsChanged(checkedPlaylists: Map<Playlist.Identifier, Playlist>)
    }

    class SimplePlaylistViewHolder(itemView: View): BaseAdapter.BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = null
    }
}