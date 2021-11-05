package com.frolo.muse.engine.service.observers

import com.frolo.muse.common.map
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.newSingleThreadExecutor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


/**
 * Observes the state of the player state and saves its changes to the current queue,
 * current item, shuffle and repeat modes in [preferences],
 * thus, the state can be restored for the player later.
 */
class PlayerStateSaver constructor(
    private val preferences: Preferences
): SimplePlayerObserver() {

    private val workerExecutor: Executor by lazy {
        newSingleThreadExecutor("PlayerStateSaver")
    }
    private val workerScheduler: Scheduler by lazy {
        Schedulers.from(workerExecutor)
    }
    private val computationScheduler: Scheduler get() = Schedulers.computation()

    private val internalDisposables = CompositeDisposable()

    // Ref to the playback progress observer
    private val playbackProgressObserverRef = AtomicReference<Disposable>()

    // Ref to the current queue
    private val queueRef = AtomicReference<AudioSourceQueue>(null)

    // Queue callback to save queue changes
    private val queueCallback = AudioSourceQueue.Callback { queue ->
        saveQueueAsync(queue)
    }

    override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
        queueRef.getAndSet(queue)?.unregisterCallback(queueCallback)
        queue.registerCallback(queueCallback, workerExecutor)
        saveQueueAsync(queue)
    }

    private fun saveQueueAsync(queue: AudioSourceQueue) {
        Single.fromCallable { queue.map { source -> source.id } }
            .subscribeOn(computationScheduler)
            .flatMapCompletable { preferences.saveLastMediaCollectionItemIds(it) }
            .doOnComplete {
                preferences.apply {
                    saveLastMediaCollectionType(-1)
                    saveLastMediaCollectionId(-1)
                }
            }
            .subscribe()
            .also { newDisposable ->
                internalDisposables.add(newDisposable)
            }
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        Completable.fromAction { preferences.saveLastSongId(item?.id ?: -1L) }
            .subscribeOn(workerScheduler)
            .subscribe()
            .also { newDisposable ->
                internalDisposables.add(newDisposable)
            }
    }

    override fun onPlaybackStarted(player: Player) {
        Observable.interval(1, TimeUnit.SECONDS, computationScheduler)
            .timeInterval()
            .observeOn(workerScheduler)
            .doOnNext {
                val progress = player.getProgress()
                preferences.saveLastPlaybackPosition(progress)
            }
            .subscribe()
            .also { newDisposable ->
                internalDisposables.add(newDisposable)
                playbackProgressObserverRef.getAndSet(newDisposable)?.dispose()
            }
    }

    override fun onPlaybackPaused(player: Player) {
        playbackProgressObserverRef.getAndSet(null)?.dispose()
    }

    override fun onSoughtTo(player: Player, position: Int) {
        Completable.fromAction { preferences.saveLastPlaybackPosition(position) }
            .subscribeOn(workerScheduler)
            .subscribe()
            .also { newDisposable ->
                internalDisposables.add(newDisposable)
            }
    }

    override fun onRepeatModeChanged(player: Player, mode: Int) {
        Completable.fromAction { preferences.saveRepeatMode(mode) }
            .subscribeOn(workerScheduler)
            .subscribe()
            .also { newDisposable ->
                internalDisposables.add(newDisposable)
            }
    }

    override fun onShuffleModeChanged(player: Player, mode: Int) {
        Completable.fromAction { preferences.saveShuffleMode(mode) }
            .subscribeOn(workerScheduler)
            .subscribe()
            .also { newDisposable ->
                internalDisposables.add(newDisposable)
            }
    }

    override fun onShutdown(player: Player) {
        internalDisposables.dispose()
        playbackProgressObserverRef.getAndSet(null)?.dispose()
        queueRef.getAndSet(null)?.unregisterCallback(queueCallback)
    }

}