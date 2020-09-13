package com.frolo.muse.engine.service

import android.graphics.Bitmap
import com.frolo.muse.engine.AudioSource


/**
 * Represents player notification model. This includes the current playing [item], the album [art]
 * and the [isPlaying] flag, which determines whether the player is currently playing.
 * If [item] is null, it means that there is no playing song in the player.
 */
data class PlayerNtf(
    val item: AudioSource?,
    val art: Bitmap?,
    val isPlaying: Boolean,
    val isFavourite: Boolean
) {

    companion object {

        val NONE = PlayerNtf(item = null, art = null, isPlaying = false, isFavourite = false)

    }

}