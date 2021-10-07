package com.frolo.muse.engine.service.observers

import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.interactor.media.DispatchSongPlayedUseCase
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.stopwatch.Stopwatch
import com.frolo.muse.stopwatch.Stopwatches
import io.reactivex.disposables.CompositeDisposable


/**
 * The logic of counting song plays is implemented here.
 * A song is considered played if:
 * 1) the player performed it entirely without shifting the playback progress;
 * 2) the player performed one or several chunks of the song and in summary, the duration of these chunks is at least [MIN_VALUABLE_DURATION] milliseconds;
 * NOTE: If song's duration is less than [MIN_VALUABLE_DURATION] then it's enough to perform 90% of the song.
 */
class SongPlayCounter constructor(
    private val schedulerProvider: SchedulerProvider,
    private val dispatchSongPlayedUseCase: DispatchSongPlayedUseCase
): SimplePlayerObserver() {

    private val internalDisposables = CompositeDisposable()

    private var currentItem: AudioSource? = null
    private var currentItemDuration: Int = 0

    private val stopwatch: Stopwatch = Stopwatches.createSimple()
    private var wasCurrentItemChecked: Boolean = false

    private fun checkIfPlayed() {
        val playedTime = stopwatch.elapsedTime

        val isPlayed = when {
            // If audio's duration is so negligible then ignore it
            currentItemDuration < NEGLIGIBLE_DURATION -> false

            currentItemDuration < MIN_VALUABLE_DURATION -> {
                playedTime >= currentItemDuration * 0.9 // 90% of audio's duration
            }

            else -> {
                val percent = playedTime.toFloat() / currentItemDuration
                percent >= MIN_VALUABLE_PERCENT_OF_DURATION && playedTime >= MIN_VALUABLE_DURATION * 0.9
            }
        }

        if (isPlayed && !wasCurrentItemChecked) {
            wasCurrentItemChecked = true
            currentItem?.also { safeItem ->
                dispatchSongPlayedUseCase.dispatchSongPlayed(safeItem.toSong())
                    .observeOn(schedulerProvider.main())
                    .doOnSubscribe { internalDisposables.add(it) }
                    .subscribe()
            }
        }
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        checkIfPlayed()

        currentItem = item
        currentItemDuration = 0
        stopwatch.stop()
        wasCurrentItemChecked = false
    }

    override fun onPrepared(player: Player, duration: Int, progress: Int) {
        currentItemDuration = duration
    }

    override fun onPlaybackStarted(player: Player) {
        stopwatch.start()
    }

    override fun onPlaybackPaused(player: Player) {
        stopwatch.pause()
    }

    override fun onShutdown(player: Player) {
        checkIfPlayed()

        // Clearing states
        currentItem = null
        currentItemDuration = 0
        stopwatch.stop()
        wasCurrentItemChecked = false

        // Disposing the other stuff
        internalDisposables.clear()
    }

    companion object {
        private const val NEGLIGIBLE_DURATION = 0.01f

        private const val MIN_VALUABLE_DURATION = 5_000 // 5 seconds

        private const val MIN_VALUABLE_PERCENT_OF_DURATION = 0.3f // 30% of duration
    }

}