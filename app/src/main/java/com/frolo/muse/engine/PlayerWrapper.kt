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

    private val delegate = AtomicReference<Player>()

    fun attachBase(player: Player) {
        delegate.set(player)
    }

    fun detachBase() {
        delegate.set(null)
    }

    override fun registerObserver(observer: PlayerObserver) {
        delegate.get()?.registerObserver(observer)
    }

    override fun unregisterObserver(observer: PlayerObserver) {
        delegate.get()?.unregisterObserver(observer)
    }

    override fun prepare(queue: AudioSourceQueue, item: AudioSource, startPlaying: Boolean) {
        delegate.get()?.prepare(queue, item, startPlaying)
    }

    override fun prepare(queue: AudioSourceQueue, item: AudioSource, playbackPosition: Int, startPlaying: Boolean) {
        delegate.get()?.prepare(queue, item, playbackPosition, startPlaying)
    }

    override fun shutdown() {
        delegate.get()?.shutdown()
    }

    override fun skipToPrevious() {
        delegate.get()?.skipToPrevious()
    }

    override fun skipToNext() {
        delegate.get()?.skipToNext()
    }

    override fun skipTo(position: Int, forceStartPlaying: Boolean) {
        delegate.get()?.skipTo(position, forceStartPlaying)
    }

    override fun skipTo(item: AudioSource, forceStartPlaying: Boolean) {
        delegate.get()?.skipTo(item, forceStartPlaying)
    }

    override fun isPrepared(): Boolean {
        return delegate.get()?.isPrepared() ?: false
    }

    override fun isPlaying(): Boolean {
        return delegate.get()?.isPlaying() ?: false
    }

    override fun getAudiSessionId(): Int {
        return delegate.get()?.getAudiSessionId() ?: -1
    }

    override fun getCurrent(): AudioSource? {
        return delegate.get()?.getCurrent()
    }

    override fun getCurrentPositionInQueue(): Int {
        return delegate.get()?.getCurrentPositionInQueue() ?: -1
    }

    override fun getCurrentQueue(): AudioSourceQueue? {
        return delegate.get()?.getCurrentQueue()
    }

    override fun getProgress(): Int {
        return delegate.get()?.getProgress() ?: 0
    }

    override fun seekTo(position: Int) {
        delegate.get()?.seekTo(position)
    }

    override fun getDuration(): Int {
        return delegate.get()?.getDuration() ?: 0
    }

    override fun start() {
        delegate.get()?.start()
    }

    override fun pause() {
        delegate.get()?.pause()
    }

    override fun toggle() {
        delegate.get()?.toggle()
    }

    override fun update(item: AudioSource) {
        delegate.get()?.update(item)
    }

    override fun remove(position: Int) {
        delegate.get()?.remove(position)
    }

    override fun removeAll(items: Collection<AudioSource>) {
        delegate.get()?.removeAll(items)
    }

    override fun add(item: AudioSource) {
        delegate.get()?.add(item)
    }

    override fun addAll(items: List<AudioSource>) {
        delegate.get()?.addAll(items)
    }

    override fun addNext(item: AudioSource) {
        delegate.get()?.addNext(item)
    }

    override fun addAllNext(items: List<AudioSource>) {
        delegate.get()?.addAllNext(items)
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        delegate.get()?.moveItem(fromPosition, toPosition)
    }

    override fun getAudioFx(): AudioFx {
        return delegate.get()?.getAudioFx() ?: AudioFxStub
    }

    override fun isAPointed(): Boolean {
        return delegate.get()?.isAPointed() ?: false
    }

    override fun isBPointed(): Boolean {
        return delegate.get()?.isBPointed() ?: false
    }

    override fun pointA(position: Int) {
        delegate.get()?.pointA(position)
    }

    override fun pointB(position: Int) {
        delegate.get()?.pointB(position)
    }

    override fun resetAB() {
        delegate.get()?.resetAB()
    }

    override fun rewindForward(interval: Int) {
        delegate.get()?.rewindForward(interval)
    }

    override fun rewindBackward(interval: Int) {
        delegate.get()?.rewindBackward(interval)
    }

    override fun getCrossFadeStrategy(): CrossFadeStrategy? {
        return delegate.get()?.getCrossFadeStrategy()
    }

    override fun setCrossFadeStrategy(strategy: CrossFadeStrategy?) {
        delegate.get()?.setCrossFadeStrategy(strategy)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getSpeed(): Float {
        return delegate.get()?.getSpeed() ?: SPEED_NORMAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setSpeed(speed: Float) {
        delegate.get()?.setSpeed(speed)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getPitch(): Float {
        return delegate.get()?.getPitch() ?: PITCH_NORMAL
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun setPitch(pitch: Float) {
        delegate.get()?.setPitch(pitch)
    }

    override fun getShuffleMode(): Int {
        return delegate.get()?.getShuffleMode() ?: SHUFFLE_OFF
    }

    override fun setShuffleMode(mode: Int) {
        delegate.get()?.setShuffleMode(mode)
    }

    override fun getRepeatMode(): Int {
        return delegate.get()?.getRepeatMode() ?: REPEAT_OFF
    }

    override fun setRepeatMode(mode: Int) {
        delegate.get()?.setRepeatMode(mode)
    }

}