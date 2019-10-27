package com.frolo.muse.engine

import android.os.Build
import androidx.annotation.RequiresApi
import com.frolo.muse.engine.Player.Companion.PITCH_NORMAL
import com.frolo.muse.engine.Player.Companion.REPEAT_OFF
import com.frolo.muse.engine.Player.Companion.SHUFFLE_OFF
import com.frolo.muse.engine.Player.Companion.SPEED_NORMAL
import com.frolo.muse.engine.stub.AudioFxStub
import com.frolo.muse.model.media.Song


class PlayerWrapper : Player {

    private var origin: Player? = null

    fun attachOrigin(origin: Player) {
        this.origin = origin
    }

    fun detachOrigin() {
        this.origin = null
    }

    override fun registerObserver(observer: PlayerObserver) {
        origin?.registerObserver(observer)
    }

    override fun unregisterObserver(observer: PlayerObserver) {
        origin?.unregisterObserver(observer)
    }

    override fun prepare(queue: SongQueue, song: Song, startPlaying: Boolean) {
        origin?.prepare(queue, song, startPlaying)
    }

    override fun prepare(queue: SongQueue, song: Song, playbackPosition: Int, startPlaying: Boolean) {
        origin?.prepare(queue, song, playbackPosition, startPlaying)
    }

    override fun shutdown() {
        origin?.shutdown()
    }

    override fun skipToPrevious() {
        origin?.skipToPrevious()
    }

    override fun skipToNext() {
        origin?.skipToNext()
    }

    override fun skipTo(position: Int, forceStartPlaying: Boolean) {
        origin?.skipTo(position, forceStartPlaying)
    }

    override fun skipTo(song: Song, forceStartPlaying: Boolean) {
        origin?.skipTo(song, forceStartPlaying)
    }

    override fun isPrepared(): Boolean {
        return origin?.isPrepared() ?: false
    }

    override fun isPlaying(): Boolean {
        return origin?.isPlaying() ?: false
    }

    override fun getAudiSessionId(): Int {
        return origin?.getAudiSessionId() ?: -1
    }

    override fun getCurrent(): Song? {
        return origin?.getCurrent()
    }

    override fun getCurrentPositionInQueue(): Int {
        return origin?.getCurrentPositionInQueue() ?: -1
    }

    override fun getCurrentQueue(): SongQueue? {
        return origin?.getCurrentQueue()
    }

    override fun getProgress(): Int {
        return origin?.getProgress() ?: 0
    }

    override fun seekTo(position: Int) {
        origin?.seekTo(position)
    }

    override fun getDuration(): Int {
        return origin?.getDuration() ?: 0
    }

    override fun start() {
        origin?.start()
    }

    override fun pause() {
        origin?.pause()
    }

    override fun toggle() {
        origin?.toggle()
    }

    override fun update(song: Song) {
        origin?.update(song)
    }

    override fun remove(position: Int) {
        origin?.remove(position)
    }

    override fun removeAll(songs: Collection<Song>) {
        origin?.removeAll(songs)
    }

    override fun add(song: Song) {
        origin?.add(song)
    }

    override fun addAll(songs: List<Song>) {
        origin?.addAll(songs)
    }

    override fun addNext(song: Song) {
        origin?.addNext(song)
    }

    override fun addAllNext(songs: List<Song>) {
        origin?.addAllNext(songs)
    }

    override fun swap(fromPosition: Int, toPosition: Int) {
        origin?.swap(fromPosition, toPosition)
    }

    override fun getAudioFx(): AudioFx {
        return origin?.getAudioFx() ?: AudioFxStub
    }

    override fun isAPointed(): Boolean {
        return origin?.isAPointed() ?: false
    }

    override fun isBPointed(): Boolean {
        return origin?.isBPointed() ?: false
    }

    override fun pointA(position: Int) {
        origin?.pointA(position)
    }

    override fun pointB(position: Int) {
        origin?.pointB(position)
    }

    override fun resetAB() {
        origin?.resetAB()
    }

    override fun rewindForward(interval: Int) {
        origin?.rewindForward(interval)
    }

    override fun rewindBackward(interval: Int) {
        origin?.rewindBackward(interval)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getSpeed(): Float {
        return origin?.getSpeed() ?: SPEED_NORMAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setSpeed(speed: Float) {
        origin?.setSpeed(speed)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getPitch(): Float {
        return origin?.getPitch() ?: PITCH_NORMAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setPitch(pitch: Float) {
        origin?.setPitch(pitch)
    }

    override fun getShuffleMode(): Int {
        return origin?.getShuffleMode() ?: SHUFFLE_OFF
    }

    override fun setShuffleMode(mode: Int) {
        origin?.setShuffleMode(mode)
    }

    override fun getRepeatMode(): Int {
        return origin?.getRepeatMode() ?: REPEAT_OFF
    }

    override fun setRepeatMode(mode: Int) {
        origin?.setRepeatMode(mode)
    }

}