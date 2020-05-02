package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.item_select_song.view.*


class SongSelectorAdapter constructor(
        requestManager: RequestManager
): SongAdapter<Song>(requestManager) {

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int) =
            SongSelectorViewHolder(parent.inflateChild(R.layout.item_select_song))

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: Song,
        selected: Boolean, selectionChanged: Boolean
    ) {
        val selectorViewHolder = holder as SongSelectorViewHolder
        with(selectorViewHolder.itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_album_name.text = item.getAlbumString(res)
            tv_duration.text = item.getDurationString()

            chb_select_song.setChecked(checked = selected, animate = selectionChanged)

            isSelected = selected
        }
    }

    class SongSelectorViewHolder(itemView: View): SongViewHolder(itemView) {
        override val viewOptionsMenu: View? = null

        init {
            val drawable =
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_framed_music_note_48dp)
            itemView.chb_select_song.setImageDrawable(drawable)
        }
    }

}