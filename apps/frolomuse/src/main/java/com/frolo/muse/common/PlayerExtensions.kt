package com.frolo.muse.common

import com.frolo.player.Player

/**
 * 10 seconds for the fast seek forward / backward.
 */
private const val FAST_SEEK_SHIFT = 10_000

fun Player.switchToNextRepeatMode() {
    val nextMode = when(getRepeatMode()) {
        Player.REPEAT_OFF -> Player.REPEAT_PLAYLIST
        Player.REPEAT_PLAYLIST -> Player.REPEAT_ONE
        Player.REPEAT_ONE -> Player.REPEAT_OFF
        else -> Player.REPEAT_OFF
    }
    setRepeatMode(nextMode)
}

fun Player.switchToNextShuffleMode() {
    val nextMode = when(getShuffleMode()) {
        Player.SHUFFLE_OFF -> Player.SHUFFLE_ON
        Player.SHUFFLE_ON -> Player.SHUFFLE_OFF
        else -> Player.SHUFFLE_OFF
    }
    setShuffleMode(nextMode)
}

fun Player.pointNextABPoint() {
    val position = getProgress()
    when {
        isAPointed().not() -> pointA(position)
        isBPointed().not() -> pointB(position)
        else -> resetAB()
    }
}

fun Player.rewindForward() {
    this.rewindForward(FAST_SEEK_SHIFT)
}

fun Player.rewindBackward() {
    this.rewindBackward(FAST_SEEK_SHIFT)
}