package com.frolo.muse.ui.main.library.myfiles

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.music.model.MyFile
import com.frolo.muse.thumbnails.ThumbnailLoader
import com.frolo.muse.ui.base.PlayStateAwareAdapter
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_file.view.*


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

        with(holder.itemView as MediaConstraintLayout) {
            tv_filename.text = item.getNameString()

            thumbnailLoader.loadMyFileThumbnail(item, imv_song_thumbnail)

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

    class MyFileViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View?
            get() = itemView.view_options_menu
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<MyFile>() {
        override fun areItemsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
            return oldItem.javaFile?.absolutePath == newItem.javaFile?.absolutePath
        }

        override fun areContentsTheSame(oldItem: MyFile, newItem: MyFile): Boolean {
            return oldItem.javaFile.name == newItem.javaFile.name
        }

    }

}