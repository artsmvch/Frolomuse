package com.frolo.core.ui.carousel

import com.frolo.player.AudioSource

interface ICarouselView {
    val size: Int
    val isEmpty: Boolean get() = size == 0

    fun registerCallback(callback: CarouselCallback)
    fun unregisterCallback(callback: CarouselCallback)

    /**
     * Invalidates the current data and requests re-render for it.
     */
    fun invalidateData()

    /**
     * Sets the current data. Note: this operation may be asynchronous,
     * [commitCallback] will be fired when the data is set.
     */
    fun submitList(list: List<AudioSource>?, commitCallback: Runnable? = null)

    /**
     * Sets the current position of the carousel.
     */
    fun setCurrentPosition(position: Int)

    interface CarouselCallback {
        fun onPositionSelected(position: Int, byUser: Boolean)
    }
}