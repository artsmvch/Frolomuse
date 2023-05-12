package com.frolo.player

import androidx.annotation.IntDef


interface Player {
    fun registerObserver(observer: PlayerObserver)
    fun unregisterObserver(observer: PlayerObserver)
    fun prepareByTarget(
        queue: AudioSourceQueue,
        target: AudioSource,
        startPlaying: Boolean,
        playbackPosition: Int
    )
    fun prepareByPosition(
        queue: AudioSourceQueue,
        positionInQueue: Int,
        startPlaying: Boolean,
        playbackPosition: Int
    )
    fun isShutdown(): Boolean
    fun shutdown()
    fun skipToPrevious()
    fun skipToNext()
    fun skipTo(position: Int, forceStartPlaying: Boolean)
    fun skipTo(item: AudioSource, forceStartPlaying: Boolean)
    fun isPrepared(): Boolean
    fun isPlaying(): Boolean
    fun getAudiSessionId(): Int
    fun getCurrent(): AudioSource?
    fun getCurrentPositionInQueue(): Int
    fun getCurrentQueue(): AudioSourceQueue?
    fun getProgress(): Int
    fun seekTo(position: Int)
    fun getDuration(): Int
    fun start()
    fun pause()
    fun toggle()
    fun update(item: AudioSource)
    fun removeAt(position: Int)
    fun removeAll(items: Collection<AudioSource>)
    fun add(item: AudioSource)
    fun addAll(items: List<AudioSource>)
    fun addNext(item: AudioSource)
    fun addAllNext(items: List<AudioSource>)
    fun moveItem(fromPosition: Int, toPosition: Int)
    // AB Controller
    fun getABController(): ABController?
    // Rewind
    fun rewindForward(interval: Int)
    fun rewindBackward(interval: Int)
    // Playback Fading
    fun getPlaybackFadingStrategy(): PlaybackFadingStrategy?
    fun setPlaybackFadingStrategy(strategy: PlaybackFadingStrategy?)

    fun getSpeed(): Float
    fun setSpeed(speed: Float)
    fun getPitch(): Float
    fun setPitch(pitch: Float)

    @ShuffleMode
    fun getShuffleMode(): Int
    fun setShuffleMode(@ShuffleMode mode: Int)
    @RepeatMode
    fun getRepeatMode(): Int
    fun setRepeatMode(@RepeatMode mode: Int)

    @IntDef(SHUFFLE_OFF, SHUFFLE_ON)
    @Retention(AnnotationRetention.SOURCE)
    @Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.LOCAL_VARIABLE
    )
    annotation class ShuffleMode

    @IntDef(REPEAT_OFF, REPEAT_ONE, REPEAT_PLAYLIST)
    @Retention(AnnotationRetention.SOURCE)
    @Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.LOCAL_VARIABLE
    )
    annotation class RepeatMode

    interface ABController {
        val isPointASet: Boolean
        val isPointBSet: Boolean
        fun setPointA()
        fun setPointB()
        fun reset()
    }

    companion object {
        const val REPEAT_OFF = 0
        const val REPEAT_ONE = 1
        const val REPEAT_PLAYLIST = 2

        const val SHUFFLE_OFF = 3
        const val SHUFFLE_ON = 4

        const val SPEED_NORMAL = 1f
        const val PITCH_NORMAL = 1f
    }
}