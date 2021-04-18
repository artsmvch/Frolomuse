package com.frolo.muse.ui.main.library.buckets.files

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import com.frolo.muse.model.media.MediaFile
import com.frolo.muse.ui.getNameString
import com.frolo.muse.ui.main.library.base.BaseAdapter
import com.frolo.muse.ui.main.library.base.sectionIndexAt
import com.frolo.muse.views.media.MediaConstraintLayout
import com.l4digital.fastscroll.FastScroller
import kotlinx.android.synthetic.main.include_check.view.*
import kotlinx.android.synthetic.main.include_song_art_container.view.*
import kotlinx.android.synthetic.main.item_media_file.view.*
import kotlinx.android.synthetic.main.item_media_file.view.view_options_menu


class AudioFileAdapter : BaseAdapter<MediaFile, AudioFileAdapter.ViewHolder>(), FastScroller.SectionIndexer {

    var playingPosition = -1
        private set
    var isPlaying = false
        private set

    fun submit(list: List<MediaFile>, position: Int, isPlaying: Boolean) {
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

    override fun onPreMove(fromPosition: Int, toPosition: Int) {
        when (playingPosition) {
            fromPosition -> playingPosition = toPosition
            in (fromPosition + 1)..toPosition -> playingPosition--
            in toPosition until fromPosition -> playingPosition++
        }
    }

    override fun getSectionText(position: Int) = sectionIndexAt(position) { getNameString() }

    override fun onCreateBaseViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_media_file)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: MediaFile, selected: Boolean, selectionChanged: Boolean) {
        with(holder.itemView as MediaConstraintLayout) {
            tv_name.text = item.getNameString()
            imv_album_art.setImageResource(R.drawable.ic_framed_music_note)

            val isPlayPosition = position == playingPosition

            if (isPlayPosition) {
                view_song_art_overlay.isVisible = true
                mini_visualizer.isVisible = true
                mini_visualizer.setAnimate(isPlaying)
            } else {
                view_song_art_overlay.isVisible = false
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

}