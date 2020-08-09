package com.frolo.muse.engine

import android.os.Build
import androidx.annotation.RequiresApi
import com.frolo.muse.engine.Player.Companion.PITCH_NORMAL
import com.frolo.muse.engine.Player.Companion.REPEAT_OFF
import com.frolo.muse.engine.Player.Companion.SHUFFLE_OFF
import com.frolo.muse.engine.Player.Companion.SPEED_NORMAL
import com.frolo.muse.engine.stub.AudioFxStub
import java.util.concurrent.atomic.AtomicReference


/**
 * Thread-safe wrapper for [Player] that delegates all method calls to [delegate].
 * The delegate can be attached using [attachBase] method and detached using [detachBase] method.
 */
class PlayerWrapper : Player {

    private val delegateRef = AtomicReference<Player>()
    private val delegate: Player? get() = delegateRef.get()

    fun attachBase(player: Player) {
        delegateRef.set(player)
    }

    fun detachBase() {
        delegateRef.set(null)
    }

    override fun registerObserver(observer: PlayerObserver) {
        delegate?.registerObserver(observer)
    }

    override fun unregisterObserver(observer: PlayerObserver) {
        delegate?.unregisterObserver(observer)
    }

    override fun prepare(queue: AudioSourceQueue, item: AudioSource, startPlaying: Boolean) {
        delegate?.prepare(queue, item, startPlaying)
    }

    override fun prepare(queue: AudioSourceQueue, item: AudioSource, playbackPosition: Int, startPlaying: Boolean) {
        delegate?.prepare(queue, item, playbackPosition, startPlaying)
    }

    override fun shutdown() {
        delegate?.shutdown()
    }

    override fun skipToPrevious() {
        delegate?.skipToPrevious()
    }

    override fun skipToNext() {
        delegate?.skipToNext()
    }

    override fun skipTo(position: Int, forceStartPlaying: Boolean) {
        delegate?.skipTo(position, forceStartPlaying)
    }

    override fun skipTo(item: AudioSource, forceStartPlaying: Boolean) {
        delegate?.skipTo(item, forceStartPlaying)
    }

    override fun isPrepared(): Boolean {
        return delegate?.isPrepared() ?: false
    }

    override fun isPlaying(): Boolean {
        return delegate?.isPlaying() ?: false
    }

    override fun getAudiSessionId(): Int {
        return delegate?.getAudiSessionId() ?: -1
    }

    override fun getCurrent(): AudioSource? {
        return delegate?.getCurrent()
    }

    override fun getCurrentPositionInQueue(): Int {
        return delegate?.getCurrentPositionInQueue() ?: -1
    }

    override fun getCurrentQueue(): AudioSourceQueue? {
        return delegate?.getCurrentQueue()
    }

    override fun getProgress(): Int {
        return delegate?.getProgress() ?: 0
    }

    override fun seekTo(position: Int) {
        delegate?.seekTo(position)
    }

    override fun getDuration(): Int {
        return delegate?.getDuration() ?: 0
    }

    override fun start() {
        delegate?.start()
    }

    override fun pause() {
        delegate?.pause()
    }

    override fun toggle() {
        delegate?.toggle()
    }

    override fun update(item: AudioSource) {
        delegate?.update(item)
    }

    override fun remove(position: Int) {
        delegate?.remove(position)
    }

    override fun removeAll(items: Collection<AudioSource>) {
        delegate?.removeAll(items)
    }

    override fun add(item: AudioSource) {
        delegate?.add(item)
    }

    override fun addAll(items: List<AudioSource>) {
        delegate?.addAll(items)
    }

    override fun addNext(item: AudioSource) {
        delegate?.addNext(item)
    }

    override fun addAllNext(items: List<AudioSource>) {
        delegate?.addAllNext(items)
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        delegate?.moveItem(fromPosition, toPosition)
    }

    override fun getAudioFx(): AudioFx {
        return delegate?.getAudioFx() ?: AudioFxStub
    }

    override fun isAPointed(): Boolean {
        return delegate?.isAPointed() ?: false
    }

    override fun isBPointed(): Boolean {
        return delegate?.isBPointed() ?: false
    }

    override fun pointA(position: Int) {
        delegate?.pointA(position)
    }

    override fun pointB(position: Int) {
        delegate?.pointB(position)
    }

    override fun resetAB() {
        delegate?.resetAB()
    }

    override fun rewindForward(interval: Int) {
        delegate?.rewindForward(interval)
    }

    override fun rewindBackward(interval: Int) {
        delegate?.rewindBackward(interval)
    }

    override fun getPlaybackFadingStrategy(): PlaybackFadingStrategy? {
        return delegate?.getPlaybackFadingStrategy()
    }

    override fun setPlaybackFadingStrategy(strategy: PlaybackFadingStrategy?) {
        delegate?.setPlaybackFadingStrategy(strategy)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getSpeed(): Float {
        return delegate?.getSpeed() ?: SPEED_NORMAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setSpeed(speed: Float) {
        delegate?.setSpeed(speed)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getPitch(): Float {
        return delegate?.getPitch() ?: PITCH_NORMAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setPitch(pitch: Float) {
        delegate?.setPitch(pitch)
    }

    override fun getShuffleMode(): Int {
        return delegate?.getShuffleMode() ?: SHUFFLE_OFF
    }

    override fun setShuffleMode(mode: Int) {
        delegate?.setShuffleMode(mode)
    }

    override fun getRepeatMode(): Int {
        return delegate?.getRepeatMode() ?: REPEAT_OFF
    }

    override fun setRepeatMode(mode: Int) {
        delegate?.setRepeatMode(mode)
    }

}