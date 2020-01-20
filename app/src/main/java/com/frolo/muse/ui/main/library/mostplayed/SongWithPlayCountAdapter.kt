package com.frolo.muse.ui.main.library.mostplayed

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.frolo.muse.GlideManager
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.SongWithPlayCount
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_song_with_play_count.view.*


class SongWithPlayCountAdapter constructor(
        private val requestManager: RequestManager
): SongAdapter<SongWithPlayCount>(requestManager) {

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int) =
        SongWithPlayCountViewHolder(
            parent.inflateChild(R.layout.item_song_with_play_count)
        )

    override fun onBindViewHolder(
            holder: SongViewHolder,
            position: Int,
            item: SongWithPlayCount,
            selected: Boolean,
            selectionChanged: Boolean
    ) {
        with(holder.itemView) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.getDurationString()
            tv_play_count.text = res.getQuantityString(R.plurals.played_s_times, item.playCount, item.playCount)

            val options = GlideManager.get()
                    .requestOptions(item.albumId)
                    .placeholder(R.drawable.ic_note_rounded_placeholder)
            requestManager
                    .load(GlideManager.albumArtUri(item.albumId))
                    .apply(options)
                    .circleCrop()
                    .into(imv_album_art)

            if (position != playingPosition) {
                mini_visualizer.visibility = View.INVISIBLE
                mini_visualizer.setAnimating(false)
            } else {
                mini_visualizer.visibility = View.VISIBLE
                mini_visualizer.setAnimating(isPlaying)
            }

            imv_check.setChecked(selected, selectionChanged)

            isSelected = selected
        }
    }

    class SongWithPlayCountViewHolder(itemView: View): SongViewHolder(itemView) {
        override val viewOptionsMenu: View?
            get() = itemView.view_options_menu
    }

}