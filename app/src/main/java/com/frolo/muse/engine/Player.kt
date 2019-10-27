package com.frolo.muse.engine

import android.os.Build
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import com.frolo.muse.model.media.Song


interface Player {
    fun registerObserver(observer: PlayerObserver)
    fun unregisterObserver(observer: PlayerObserver)
    fun prepare(queue: SongQueue, song: Song, startPlaying: Boolean)
    fun prepare(queue: SongQueue, song: Song, playbackPosition: Int, startPlaying: Boolean)
    fun shutdown()
    fun skipToPrevious()
    fun skipToNext()
    fun skipTo(position: Int, forceStartPlaying: Boolean)
    fun skipTo(song: Song, forceStartPlaying: Boolean)
    fun isPrepared(): Boolean
    fun isPlaying(): Boolean
    fun getAudiSessionId(): Int
    fun getCurrent(): Song?
    fun getCurrentPositionInQueue(): Int
    fun getCurrentQueue(): SongQueue?
    fun getProgress(): Int
    fun seekTo(position: Int)
    fun getDuration(): Int
    fun start()
    fun pause()
    fun toggle()
    fun update(song: Song)
    fun remove(position: Int)
    fun removeAll(songs: Collection<Song>)
    fun add(song: Song)
    fun addAll(songs: List<Song>)
    fun addNext(song: Song)
    fun addAllNext(songs: List<Song>)
    fun swap(fromPosition: Int, toPosition: Int)
    fun getAudioFx(): AudioFx
    // AB functionality
    fun isAPointed(): Boolean
    fun isBPointed(): Boolean
    fun pointA(position: Int)
    fun pointB(position: Int)
    fun resetAB()
    // Rewind
    fun rewindForward(interval: Int)
    fun rewindBackward(interval: Int)

    @RequiresApi(Build.VERSION_CODES.M)
    fun getSpeed(): Float
    @RequiresApi(Build.VERSION_CODES.M)
    fun setSpeed(speed: Float)
    @RequiresApi(Build.VERSION_CODES.M)
    fun getPitch(): Float
    @RequiresApi(Build.VERSION_CODES.M)
    fun setPitch(pitch: Float)

    @ShuffleMode fun getShuffleMode(): Int
    fun setShuffleMode(@ShuffleMode mode: Int)
    @RepeatMode fun getRepeatMode(): Int
    fun setRepeatMode(@RepeatMode mode: Int)

    @IntDef(SHUFFLE_OFF, SHUFFLE_ON)
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE)
    annotation class ShuffleMode

    @IntDef(REPEAT_OFF, REPEAT_ONE, REPEAT_PLAYLIST)
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE)
    annotation class RepeatMode

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