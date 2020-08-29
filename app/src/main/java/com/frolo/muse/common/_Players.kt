package com.frolo.muse.common

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.engine.Player


fun Player.prepareByTarget(
    queue: AudioSourceQueue,
    target: AudioSource,
    startPlaying: Boolean
): Unit = prepareByTarget(queue, target, startPlaying, 0)

fun Player.prepareByPosition(
    queue: AudioSourceQueue,
    positionInQueue: Int,
    startPlaying: Boolean
): Unit = prepareByPosition(queue, positionInQueue, startPlaying, 0)