package com.frolo.muse.ui.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.ui.main.library.base.BaseAdapter


abstract class PlayStateAwareAdapter<E, VH: BaseAdapter.BaseViewHolder>(
    // The diff item callback must be non-null, so that
    // we can correctly determine the play position moves
    itemCallback: DiffUtil.ItemCallback<E>
) : BaseAdapter<E, VH>(itemCallback) {

    /**
     * The current play position. Must be in the range [0, [getItemCount]) or [NO_PLAY_POSITION].
     */
    var playPosition: Int = NO_PLAY_POSITION
        private set

    /**
     * Indicates whether the current play position is active (i.e. playing).
     */
    var isPlaying: Boolean = false
        private set

    // The last set value of 'play position'
    private var lastSetPlayPosition: Int? = null
    // The last set value of the 'is playing'
    private var lastSetIsPlaying: Boolean? = null

    private val playStateObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            // Shift the play position if needed.
            when (playPosition) {

                // The play position is in the moved chunk.
                in fromPosition until (fromPosition + itemCount) -> {
                    val relativePlayPositionInMovedChunk = playPosition - fromPosition
                    playPosition = toPosition + relativePlayPositionInMovedChunk
                }

                // The play position is after the moved chunk and before the target position.
                in (fromPosition + itemCount)..toPosition -> {
                    playPosition -= itemCount
                }

                in toPosition until fromPosition -> {
                    playPosition += itemCount
                }

                else -> {
                    // Do nothing.
                }
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            if (playPosition in positionStart until (positionStart + itemCount)) {
                // The play position is gone.
                playPosition = NO_PLAY_POSITION
            }
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            // Shift the play position if needed.
            if (positionStart <= playPosition) {
                playPosition += itemCount
            }
        }
    }

    init {
        registerAdapterDataObserver(playStateObserver)
    }

    final override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
    }

    final override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
    }

    fun submitAndRetainPlayState(list: List<E>) {
        // Remember the current play state
        val savedPlayPosition = lastSetPlayPosition ?: this.playPosition
        val savedIsPlaying = lastSetIsPlaying ?: this.isPlaying
        val callback = Runnable {
            // Restore the play state immediately
            setPlayStateImmediately(savedPlayPosition, savedIsPlaying)
        }
        submit(list, callback)
    }

    private fun isValidPosition(position: Int): Boolean {
        return position in 0 until itemCount
    }

    fun setPlayState(playPosition: Int, isPlaying: Boolean) {
        lastSetPlayPosition = playPosition
        lastSetIsPlaying = isPlaying
        runOnSubmit {
            setPlayStateImmediately(playPosition, isPlaying)
        }
    }

    private fun setPlayStateImmediately(playPosition: Int, isPlaying: Boolean) {
        if (this.playPosition == playPosition && this.isPlaying == isPlaying) {
            // No changes.
            return
        }

        if (this.playPosition != playPosition) {
            // The play position is changed. Need to notify about
            // change at both the old and the new positions.
            val oldPlayPosition = this.playPosition
            this.playPosition = playPosition
            this.isPlaying = isPlaying
            if (isValidPosition(oldPlayPosition)) {
                notifyItemChanged(oldPlayPosition)
            }
            if (isValidPosition(playPosition)) {
                notifyItemChanged(playPosition)
            }
        } else {
            // Only the is-playing status is changed.
            this.isPlaying = isPlaying
            if (isValidPosition(playPosition)) {
                notifyItemChanged(playPosition)
            }
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        lastSetIsPlaying = isPlaying
        runOnSubmit {
            setPlayingImmediately(isPlaying)
        }
    }

    private fun setPlayingImmediately(isPlaying: Boolean) {
        if (this.isPlaying == isPlaying) {
            // No changes.
            return
        }

        this.isPlaying = isPlaying
        if (isValidPosition(playPosition)) {
            notifyItemChanged(playPosition)
        }
    }

    companion object {
        protected const val NO_PLAY_POSITION = RecyclerView.NO_POSITION
    }

}