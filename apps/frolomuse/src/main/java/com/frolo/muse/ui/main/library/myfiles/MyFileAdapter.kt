package com.frolo.muse.ui.main.library.myfiles

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R
import com.frolo.music.model.MyFile
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.PlayStateAwareAdapter
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.MiniVisualizer
import com.frolo.muse.views.SongThumbnailView
import com.frolo.muse.views.checkable.CheckView
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller


class MyFileAdapter constructor(
    private val thumbnailLoader: ThumbnailLoader
): PlayStateAwareAdapter<MyFile, BaseAdapter.BaseViewHolder>(ItemDiffCallback()), FastScroller.SectionIndexer {

    override fun getSectionText(position: Int) = sectionIndexAt(position) { getNameString() }

    override fun onCreateBaseViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = MyFileViewHolder(parent.inflateChild(R.layout.item_file))

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        item: MyFile,
        selected: Boolean,
        selectionChanged: Boolean
    ) {
        with(holder as MyFileViewHolder) {
            tvFilename.text = item.getNameString()

            thumbnailLoader.loadMyFileThumbnail(item, imvSongThumbnail)

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

    class MyFileViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View? = itemView.findViewById(R.id.view_options_menu)
        val tvFilename: TextView = itemView.findViewById(R.id.tv_filename)
        val imvSongThumbnail: SongThumbnailView = itemView.findViewById(R.id.imv_song_thumbnail)
        val miniVisualizer: MiniVisualizer = itemView.findViewById(R.id.mini_visualizer)
        val imvCheck: CheckView = itemView.findViewById(R.id.imv_check)
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<MyFile>() {
        override fun areItemsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
            return oldItem.javaFile == newItem.javaFile
        }

        override fun areContentsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
            return oldItem.javaFile.name == newItem.javaFile.name
        }

    }

}