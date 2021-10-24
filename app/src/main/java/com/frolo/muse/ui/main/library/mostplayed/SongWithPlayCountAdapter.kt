package com.frolo.muse.ui.main.library.mostplayed

import android.view.View
import android.view.ViewGroup
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.SongWithPlayCount
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getLastTimePlayedString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.SongAdapter
import com.frolo.muse.views.media.MediaConstraintLayout
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_song_with_play_count.view.*


class SongWithPlayCountAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader,
): SongAdapter<SongWithPlayCount>(thumbnailLoader) {

    private companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_WITH_LAST_PLAY_TIME = 1
    }

    override fun getItemViewType(position: Int): Int {
        return getItemAt(position).let { item ->
            if (item.hasLastPlayTime()) VIEW_TYPE_WITH_LAST_PLAY_TIME
            else VIEW_TYPE_DEFAULT
        }
    }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int) =
        SongWithPlayCountViewHolder(
            itemView = parent.inflateChild(R.layout.item_song_with_play_count),
            hasLastPlayTime = viewType == VIEW_TYPE_WITH_LAST_PLAY_TIME
        )

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: SongWithPlayCount,
        selected: Boolean,
        selectionChanged: Boolean
    ) {
        val isPlayPosition = position == playingPosition

        with(holder.itemView as MediaConstraintLayout) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.getDurationString()

            if (item.hasLastPlayTime()) {
                tv_play_count.text = res.getQuantityString(R.plurals.played_s_times, item.playCount, item.playCount)
                tv_last_time_played.text = res.getString(R.string.last_time_s, item.getLastTimePlayedString(context))
            } else {
                tv_play_count.text = res.getString(R.string.not_played_yet)
                tv_last_time_played.text = null
            }

            thumbnailLoader.loadSongThumbnail(item, imv_song_thumbnail)

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
            setPlaying(isPlayPosition)
        }

        holder.resolvePlayingPosition(
            isPlayPosition = isPlayPosition,
            isPlaying = isPlaying
        )
    }

    class SongWithPlayCountViewHolder(
        itemView: View,
        hasLastPlayTime: Boolean
    ): SongViewHolder(itemView) {

        init {
            itemView.tv_last_time_played.visibility =
                if (hasLastPlayTime) View.VISIBLE else View.GONE
        }

        override val viewOptionsMenu: View? = itemView.view_options_menu
    }

}