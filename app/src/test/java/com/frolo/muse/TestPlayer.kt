package com.frolo.muse

import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerObserver
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.model.media.Song


class TestPlayer: Player {
    override fun registerObserver(observer: PlayerObserver) = Unit
    override fun unregisterObserver(observer: PlayerObserver) = Unit
    override fun prepare(queue: SongQueue, song: Song, startPlaying: Boolean) = Unit
    override fun prepare(queue: SongQueue, song: Song, playbackPosition: Int, startPlaying: Boolean) = Unit
    override fun shutdown() = Unit
    override fun skipToPrevious() = Unit
    override fun skipToNext() = Unit
    override fun skipTo(position: Int, forceStartPlaying: Boolean) = Unit
    override fun skipTo(song: Song, forceStartPlaying: Boolean) = Unit
    override fun isPrepared(): Boolean = false
    override fun isPlaying(): Boolean = false
    override fun getAudiSessionId(): Int = 0
    override fun getCurrent(): Song? = null
    override fun getCurrentPositionInQueue(): Int = -1
    override fun getCurrentQueue(): SongQueue? = null
    override fun getProgress(): Int = 0
    override fun seekTo(position: Int) = Unit
    override fun getDuration(): Int = 0
    override fun start() = Unit
    override fun pause() = Unit
    override fun toggle() = Unit
    override fun update(song: Song) = Unit
    override fun remove(position: Int) = Unit
    override fun removeAll(songs: Collection<Song>) = Unit
    override fun add(song: Song) = Unit
    override fun addAll(songs: List<Song>) = Unit
    override fun addNext(song: Song) = Unit
    override fun addAllNext(songs: List<Song>) = Unit
    override fun moveItem(fromPosition: Int, toPosition: Int) = Unit
    override fun getAudioFx(): AudioFx = TestAudioFx()
    override fun isAPointed(): Boolean = false
    override fun isBPointed(): Boolean = false
    override fun pointA(position: Int) = Unit
    override fun pointB(position: Int) = Unit
    override fun resetAB() = Unit
    override fun rewindForward(interval: Int) = Unit
    override fun rewindBackward(interval: Int) = Unit
    override fun getSpeed(): Float = 0f
    override fun setSpeed(speed: Float) = Unit
    override fun getPitch(): Float = 0f
    override fun setPitch(pitch: Float) = Unit
    override fun getShuffleMode(): Int = Player.SHUFFLE_OFF
    override fun setShuffleMode(mode: Int) = Unit
    override fun getRepeatMode(): Int = Player.REPEAT_OFF
    override fun setRepeatMode(mode: Int) = Unit

}