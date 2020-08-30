package com.frolo.muse.engine.service.observers

import android.util.Log
import com.frolo.muse.BuildConfig
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.repository.Preferences
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
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

    private val workerScheduler: Scheduler by lazy {
        val threadFactory = ThreadFactory { r -> Thread(r).apply { name = "PlayerStateSaver" } }
        val executor = Executors.newSingleThreadExecutor(threadFactory)
        Schedulers.from(executor)
    }

    private val disposables = CompositeDisposable()
    private val positionObserverDisposableRef = AtomicReference<Disposable>()

    private fun Completable.subscribeSafely(): Disposable {
        val d = subscribe(
            { /* stub */ },
            { err -> if (DEBUG) Log.e(LOG_TAG, "An error occurred", err) }
        )

        disposables.add(d)

        return d
    }

    override fun onQueueChanged(player: Player, queue: AudioSourceQueue) {
        Completable.fromAction {
            preferences.apply {
                saveLastMediaCollectionType(queue.type)
                saveLastMediaCollectionId(queue.id)
            }
        }
            .subscribeOn(workerScheduler)
            .subscribeSafely()
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        if (item != null) {
            Completable.fromAction { preferences.saveLastSongId(item.id) }
                .subscribeOn(workerScheduler)
                .subscribeSafely()
        }
    }

    override fun onPlaybackStarted(player: Player) {
        Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
            .timeInterval()
            .flatMapCompletable { doSaveLastPlaybackPosition(player) }
            .subscribeSafely()
            .also { d -> positionObserverDisposableRef.getAndSet(d)?.dispose() }
    }

    override fun onPlaybackPaused(player: Player) {
        positionObserverDisposableRef.getAndSet(null)?.dispose()
    }

    override fun onSoughtTo(player: Player, position: Int) {
        doSaveLastPlaybackPosition(player)
            .subscribeSafely()
    }

    override fun onRepeatModeChanged(player: Player, mode: Int) {
        Completable.fromAction { preferences.saveRepeatMode(mode) }
            .subscribeOn(workerScheduler)
            .subscribeSafely()
    }

    override fun onShuffleModeChanged(player: Player, mode: Int) {
        Completable.fromAction { preferences.saveShuffleMode(mode) }
            .subscribeOn(workerScheduler)
            .subscribeSafely()
    }

    override fun onShutdown(player: Player) {
        disposables.clear()
        positionObserverDisposableRef.getAndSet(null)?.dispose()
    }

    private fun doSaveLastPlaybackPosition(player: Player): Completable{
        return Completable.fromAction {
            val position = player.getProgress()
            preferences.saveLastPlaybackPosition(position)
        }.subscribeOn(workerScheduler)
    }

    companion object {

        private val DEBUG = BuildConfig.DEBUG
        private const val LOG_TAG = "PlayerStateSaver"

    }

}