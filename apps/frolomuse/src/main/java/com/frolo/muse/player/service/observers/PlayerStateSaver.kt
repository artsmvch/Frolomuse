package com.frolo.muse.player.service.observers

import com.frolo.muse.common.map
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.player.SimplePlayerObserver
import com.frolo.player.AudioSourceQueue
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
import android.content.ContentUris
import android.net.Uri

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

    /**
     * Safely extracts the media ID from a content URI.
     * Uses MediaStore ContentUris to parse the URI and returns null if the URI is invalid
     * or not a valid content URI.
     * 
     * @param uri the URI string to parse
     * @return the extracted media ID as Long, or null if the URI is invalid
     */
    private fun extractIdFromURI(uri: String): Long? {
        return try {
            val parsedUri = Uri.parse(uri)
            // Only handle content URIs
            if (parsedUri.scheme != "content") {
                return null
            }
            
            // Use ContentUris to safely extract the ID
            ContentUris.parseId(parsedUri)
        } catch (e: Exception) {
            // Return null for any parsing errors
            null
        }
    }

    private fun saveQueueAsync(queue: AudioSourceQueue) {
        Single.fromCallable { queue.map { source -> extractIdFromURI(source.getURI()) } }
            .subscribeOn(computationScheduler)
            .map { ids -> ids.mapNotNull { it } }
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
        Completable.fromAction { preferences.saveLastSongId(item?.let { extractIdFromURI(it.getURI()) } ?: -1L) }
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