package com.frolo.muse.engine


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