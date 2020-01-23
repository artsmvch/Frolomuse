package com.frolo.muse.engine.service

import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.interactor.media.DispatchSongPlayedUseCase
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable


/**
 * The logic of counting song plays is implemented here.
 * A song is considered played if:
 * -the player performed it entirely without shifting the playback progress;
 * -the player performed one or several chunks of the song and in summary, the duration of these chunks is at least [MIN_VALUABLE_DURATION] milliseconds;
 * NOTE: If song's duration is less than [MIN_VALUABLE_DURATION] then it's enough to perform 90% of the song.
 */
class SongPlayCountObserver constructor(
        private val schedulerProvider: SchedulerProvider,
        private val dispatchSongPlayedUseCase: DispatchSongPlayedUseCase
): SimplePlayerObserver() {

    private companion object {
        const val MIN_VALUABLE_DURATION = 5_000 //5 seconds

        const val PLAYBACK_NOT_STARTED_YET = -1L
    }

    private val disposables = CompositeDisposable()

    private var currentSong: Song? = null
    private var currentSongDuration: Int = 0
    private var isPrepared: Boolean = false
    private var isPlaying: Boolean = false
    // Remembers the last timestamp when the playback started
    private var lastTimePlaybackStarted: Long = PLAYBACK_NOT_STARTED_YET
    // Total duration of playback performance
    private var performanceDuration: Int = 0

    private fun now(): Long = System.currentTimeMillis()

    override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) {
        if (isPlaying) {
            // it was playing
            val performed = now() - lastTimePlaybackStarted
            performanceDuration += performed.toInt()
        }

        val isPerformed = when {
            // If it wasn't even prepared then do not consider it as played
            !isPrepared -> false

            currentSongDuration <= MIN_VALUABLE_DURATION -> {
                performanceDuration >= currentSongDuration * 0.9 // 90% of song's duration
            }

            performanceDuration >= MIN_VALUABLE_DURATION -> true

            else -> false
        }

        if (isPerformed) {
            currentSong?.also { safeSong ->
                dispatchSongPlayedUseCase.dispatchSongPlayed(safeSong)
                        .observeOn(schedulerProvider.main())
                        .subscribe({ }, { })
                        .also { d -> disposables.add(d) }
            }
        }

        currentSong = song
        currentSongDuration = 0
        isPrepared = player.isPrepared()
        isPlaying = player.isPlaying()

        lastTimePlaybackStarted = if (!isPlaying) PLAYBACK_NOT_STARTED_YET else now()
        performanceDuration = 0
    }

    override fun onPrepared(player: Player) {
        isPrepared = true
        currentSongDuration = player.getDuration()
    }

    override fun onPlaybackPaused(player: Player) {
        if (isPlaying) {
            val performed = now() - lastTimePlaybackStarted
            performanceDuration += performed.toInt()
        }
        isPlaying = false
    }

    override fun onPlaybackStarted(player: Player) {
        isPlaying = true
        lastTimePlaybackStarted = now()
    }

    override fun onShutdown(player: Player) {
        disposables.clear()
    }

}