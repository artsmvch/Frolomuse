package com.frolo.muse

import com.frolo.muse.engine.*


class TestPlayer: Player {
    override fun registerObserver(observer: PlayerObserver) = Unit
    override fun unregisterObserver(observer: PlayerObserver) = Unit
    override fun prepare(queue: AudioSourceQueue, item: AudioSource, startPlaying: Boolean) = Unit
    override fun prepare(queue: AudioSourceQueue, item: AudioSource, playbackPosition: Int, startPlaying: Boolean) = Unit
    override fun shutdown() = Unit
    override fun skipToPrevious() = Unit
    override fun skipToNext() = Unit
    override fun skipTo(position: Int, forceStartPlaying: Boolean) = Unit
    override fun skipTo(item: AudioSource, forceStartPlaying: Boolean) = Unit
    override fun isPrepared(): Boolean = false
    override fun isPlaying(): Boolean = false
    override fun getAudiSessionId(): Int = 0
    override fun getCurrent(): AudioSource? = null
    override fun getCurrentPositionInQueue(): Int = -1
    override fun getCurrentQueue(): AudioSourceQueue? = null
    override fun getProgress(): Int = 0
    override fun seekTo(position: Int) = Unit
    override fun getDuration(): Int = 0
    override fun start() = Unit
    override fun pause() = Unit
    override fun toggle() = Unit
    override fun update(item: AudioSource) = Unit
    override fun removeAt(position: Int) = Unit
    override fun removeAll(items: Collection<AudioSource>) = Unit
    override fun add(item: AudioSource) = Unit
    override fun addAll(items: List<AudioSource>) = Unit
    override fun addNext(item: AudioSource) = Unit
    override fun addAllNext(items: List<AudioSource>) = Unit
    override fun moveItem(fromPosition: Int, toPosition: Int) = Unit
    override fun getAudioFx(): AudioFx = TestAudioFx()
    override fun isAPointed(): Boolean = false
    override fun isBPointed(): Boolean = false
    override fun pointA(position: Int) = Unit
    override fun pointB(position: Int) = Unit
    override fun resetAB() = Unit
    override fun rewindForward(interval: Int) = Unit
    override fun rewindBackward(interval: Int) = Unit
    override fun getPlaybackFadingStrategy(): PlaybackFadingStrategy? = null
    override fun setPlaybackFadingStrategy(strategy: PlaybackFadingStrategy?) = Unit
    override fun getSpeed(): Float = 0f
    override fun setSpeed(speed: Float) = Unit
    override fun getPitch(): Float = 0f
    override fun setPitch(pitch: Float) = Unit
    override fun getShuffleMode(): Int = Player.SHUFFLE_OFF
    override fun setShuffleMode(mode: Int) = Unit
    override fun getRepeatMode(): Int = Player.REPEAT_OFF
    override fun setRepeatMode(mode: Int) = Unit

}