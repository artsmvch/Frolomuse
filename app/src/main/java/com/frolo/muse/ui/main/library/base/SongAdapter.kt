package com.frolo.muse.ui.main.library.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.Song
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.views.MiniVisualizer
import com.frolo.muse.views.SongThumbnailView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_song.view.*


open class SongAdapter<S: Song> constructor(
    private val thumbnailLoader: ThumbnailLoader,
    private val itemCallback: DiffUtil.ItemCallback<S>? = SongItemCallback<S>()
): BaseAdapter<S, SongAdapter.SongViewHolder>(), FastScroller.SectionIndexer {

    var playingPosition = -1
        private set
    var isPlaying = false
        private set

    override fun getItemId(position: Int) = getItemAt(position).id

    fun setPlayingPositionAndState(position: Int, isPlaying: Boolean) {
        if (this.playingPosition == position && this.isPlaying == isPlaying) {
            return
        }

        if (playingPosition >= 0) {
            this.isPlaying = false
            notifyItemChanged(playingPosition)
        }
        this.playingPosition = position
        this.isPlaying = isPlaying
        notifyItemChanged(position)
    }

    fun setPlayingState(isPlaying: Boolean) {
        if (this.isPlaying == isPlaying) {
            return
        }

        this.isPlaying = isPlaying
        if (playingPosition >= 0) {
            notifyItemChanged(playingPosition)
        }
    }

    override fun onPreRemove(position: Int) {
        if (playingPosition == position) {
            playingPosition = -1
        } else if (playingPosition > position) {
            playingPosition--
        }
    }

    override fun onPreMove(fromPosition: Int, toPosition: Int) {
        when (playingPosition) {
            fromPosition -> playingPosition = toPosition
            in (fromPosition + 1)..toPosition -> playingPosition--
            in toPosition until fromPosition -> playingPosition++
        }
    }

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = SongViewHolder(parent.inflateChild(R.layout.item_song))

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int,
        item: S,
        selected: Boolean,
        selectionChanged: Boolean
    ) {

        val isPlayPosition = position == playingPosition

        with((holder.itemView as MediaConstraintLayout)) {
            val res = resources
            tv_song_name.text = item.getNameString(res)
            tv_artist_name.text = item.getArtistString(res)
            tv_duration.text = item.getDurationString()

            thumbnailLoader.loadSongThumbnail(item, imv_song_thumbnail)

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
            setPlaying(isPlayPosition)
        }

        holder.resolvePlayingPosition(
            isPlaying = isPlaying,
            isPlayPosition = isPlayPosition
        )
    }

    override fun getSectionText(position: Int) = sectionIndexAt(position) { title }

    open class SongViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.view_options_menu

        private val songThumbnailView: SongThumbnailView? =
                itemView.findViewById(R.id.imv_song_thumbnail)
        private val miniVisualizer: MiniVisualizer? =
                itemView.findViewById(R.id.mini_visualizer)

        fun resolvePlayingPosition(
            isPlayPosition: Boolean,
            isPlaying: Boolean
        ) {
            if (isPlayPosition) {
                songThumbnailView?.isDimmed = true
                miniVisualizer?.visibility = View.VISIBLE
                miniVisualizer?.setAnimate(isPlaying)
            } else {
                songThumbnailView?.isDimmed = false
                miniVisualizer?.visibility = View.INVISIBLE
                miniVisualizer?.setAnimate(false)
            }
        }
    }

    class SongItemCallback<S: Song> : DiffUtil.ItemCallback<S>() {
        override fun areItemsTheSame(oldItem: S, newItem: S): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: S, newItem: S): Boolean {
            return oldItem == newItem
        }
    }

}