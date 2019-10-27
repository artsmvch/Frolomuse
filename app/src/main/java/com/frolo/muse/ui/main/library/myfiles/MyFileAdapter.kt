package com.frolo.muse.ui.main.library.myfiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.frolo.muse.R
import com.frolo.muse.model.media.MyFile
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.util.CharSequences
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.item_file.view.*


class MyFileAdapter: BaseAdapter<MyFile,
        BaseAdapter.BaseViewHolder>(),
        FastScroller.SectionIndexer {

    var playingPosition = -1
        private set
    var isPlaying = false
        private set

    fun submit(list: List<MyFile>, position: Int, isPlaying: Boolean) {
        this.playingPosition = position
        this.isPlaying = isPlaying
        submit(list)
    }

    fun setPlayingPositionAndState(position: Int, isPlaying: Boolean) {
        if (this.playingPosition == position
                && this.isPlaying == isPlaying) {
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
        if (playingPosition >= 0)
            notifyItemChanged(playingPosition)
    }

    override fun onPreRemove(position: Int) {
        if (playingPosition == position) {
            playingPosition = -1
        } else if (playingPosition > position) {
            playingPosition--
        }
    }

    override fun onPreSwap(fromPosition: Int, toPosition: Int) {
        when (playingPosition) {
            fromPosition -> playingPosition = toPosition
            in (fromPosition + 1)..toPosition -> playingPosition--
            in toPosition until fromPosition -> playingPosition++
        }
    }

    override fun getSectionText(position: Int): CharSequence {
        if (position >= itemCount) return CharSequences.empty()
        return getItemAt(position).getNameString().let { name ->
            CharSequences.firstCharOrEmpty(name)
        }
    }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_file, parent, false)
        return MyFileViewHolder(view)
    }

    override fun onBindViewHolder(
            holder: BaseViewHolder,
            position: Int,
            item: MyFile,
            selected: Boolean,
            selectionChanged: Boolean) {

        with(holder.itemView) {
            tv_filename.text = item.getNameString()
            when {
                item.isDirectory -> imv_file_art.setImageResource(R.drawable.ic_folder)
                item.isSongFile -> imv_file_art.setImageResource(R.drawable.ic_note_rounded_placeholder)
                else -> imv_file_art.setImageResource(R.drawable.ic_file_placeholder)
            }

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

    class MyFileViewHolder(itemView: View): BaseViewHolder(itemView) {
        override val viewOptionsMenu: View?
            get() = itemView.view_options_menu
    }

}