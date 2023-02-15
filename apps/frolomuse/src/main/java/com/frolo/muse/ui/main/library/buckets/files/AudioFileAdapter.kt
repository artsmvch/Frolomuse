package com.frolo.muse.ui.main.library.buckets.files

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.music.model.MediaFile
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.PlayStateAwareAdapter
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_media_file.view.*
import kotlinx.android.synthetic.main.item_media_file.view.view_options_menu


class AudioFileAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader
) : PlayStateAwareAdapter<MediaFile, AudioFileAdapter.ViewHolder>(ItemDiffCallback()), FastScroller.SectionIndexer {

    override fun getSectionText(position: Int) = sectionIndexAt(position) { getNameString() }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_media_file)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: MediaFile, selected: Boolean, selectionChanged: Boolean) {
        with(holder.itemView as MediaConstraintLayout) {
            tv_name.text = item.getNameString()

            thumbnailLoader.loadMediaFileThumbnail(item, imv_song_thumbnail)

            val isPlayPosition = position == playPosition

            if (isPlayPosition) {
                imv_song_thumbnail.isDimmed = true
                mini_visualizer.isVisible = true
                mini_visualizer.setAnimate(isPlaying)
            } else {
                imv_song_thumbnail.isDimmed = false
                mini_visualizer.isVisible = false
                mini_visualizer.setAnimate(false)
            }

            imv_check.setChecked(selected, selectionChanged)

            setChecked(selected)
            setPlaying(isPlayPosition)
        }
    }

    class ViewHolder(itemView: View): BaseViewHolder(itemView) {

        override val viewOptionsMenu: View? = itemView.view_options_menu

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