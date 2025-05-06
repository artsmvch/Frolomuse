package com.frolo.muse.ui.main.library.buckets.files

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.PlayStateAwareAdapter
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.MiniVisualizer
import com.frolo.muse.views.SongThumbnailView
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.frolo.music.model.MediaFile
import com.l4digital.fastscroll.FastScroller


class AudioFileAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader
) : PlayStateAwareAdapter<MediaFile, AudioFileAdapter.ViewHolder>(ItemDiffCallback()), FastScroller.SectionIndexer {

    override fun getSectionText(position: Int) = sectionIndexAt(position) { getNameString() }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_media_file)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: MediaFile, selected: Boolean, selectionChanged: Boolean) {
        with(holder) {
            tvName.text = item.getNameString()

            thumbnailLoader.loadMediaFileThumbnail(item, imvSongThumbnail)

            val isPlayPosition = position == playPosition

            if (isPlayPosition) {
                imvSongThumbnail.isDimmed = true
                miniVisualizer.isVisible = true
                miniVisualizer.setAnimate(isPlaying)
            } else {
                imvSongThumbnail.isDimmed = false
                miniVisualizer.isVisible = false
                miniVisualizer.setAnimate(false)
            }

            imvCheck.setChecked(selected, selectionChanged)

            (itemView as MediaConstraintLayout).apply {
                setChecked(selected)
                setPlaying(isPlayPosition)
            }
        }
    }

    class ViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val imvSongThumbnail: SongThumbnailView = itemView.findViewById(R.id.imv_song_thumbnail)
        val miniVisualizer: MiniVisualizer = itemView.findViewById(R.id.mini_visualizer)
        val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
            return oldItem == newItem
        }

    }

}