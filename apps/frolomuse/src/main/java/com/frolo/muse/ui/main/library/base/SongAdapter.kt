package com.frolo.muse.ui.main.library.base

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.PlayStateAwareAdapter
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getArtistString
import com.frolo.muse.ui.getDurationString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.getTrackNumberString
import com.frolo.muse.views.MiniVisualizer
import com.frolo.muse.views.SongThumbnailView
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.frolo.music.model.Song
import com.l4digital.fastscroll.FastScroller


open class SongAdapter<S: Song> constructor(
    private val thumbnailLoader: ThumbnailLoader,
    private val itemCallback: DiffUtil.ItemCallback<S> = SongItemCallback<S>()
): PlayStateAwareAdapter<S, SongAdapter.SongViewHolder>(itemCallback), FastScroller.SectionIndexer {

    override fun getItemId(position: Int) = getItemAt(position).getMediaId().getSourceId()

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
        val isPlayPosition = position == playPosition
        val res = holder.itemView.resources
        holder.tvSongName?.text = item.getNameString(res)
        holder.tvSongNumber?.text = item.getTrackNumberString(res)
        holder.tvAlbumName?.text = item.getAlbumString(res)
        holder.tvArtistName?.text = item.getArtistString(res)
        holder.tvDuration?.text = item.getDurationString()
        holder.imvCheck?.setChecked(selected, selectionChanged)
        holder.imvSongThumbnail?.also { thumbnailLoader.loadSongThumbnail(item, it) }
        (holder.itemView as? MediaConstraintLayout)?.apply {
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
        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)

        private val songThumbnailView: SongThumbnailView? =
                itemView.findViewById(R.id.imv_song_thumbnail)
        private val miniVisualizer: MiniVisualizer? =
                itemView.findViewById(R.id.mini_visualizer)

        val tvSongName: TextView? = itemView.findViewById(R.id.tv_song_name)
        val tvSongNumber: TextView? = itemView.findViewById(R.id.tv_song_number)
        val tvAlbumName: TextView? = itemView.findViewById(R.id.tv_album_name)
        val tvArtistName: TextView? = itemView.findViewById(R.id.tv_artist_name)
        val tvDuration: TextView? = itemView.findViewById(R.id.tv_duration)
        val imvCheck: CheckView? = itemView.findViewById(R.id.imv_check)
        val imvSongThumbnail: ImageView? = itemView.findViewById(R.id.imv_song_thumbnail)

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

    private class SongItemCallback<S: Song> : DiffUtil.ItemCallback<S>() {
        override fun areItemsTheSame(oldItem: S, newItem: S): Boolean {
            return oldItem.getMediaId().getSourceId() == newItem.getMediaId().getSourceId()
        }

        override fun areContentsTheSame(oldItem: S, newItem: S): Boolean {
            return oldItem.duration == newItem.duration &&
                oldItem.albumId == newItem.albumId &&
                oldItem.title == newItem.title &&
                oldItem.artist == newItem.artist
        }
    }

}