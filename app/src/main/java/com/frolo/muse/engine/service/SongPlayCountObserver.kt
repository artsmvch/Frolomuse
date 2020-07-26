package com.frolo.muse.engine.service

import com.frolo.muse.common.toSong
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.interactor.media.DispatchSongPlayedUseCase
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable


/**
 * The logic of counting song plays is implemented here.
 * A song is considered played if:
 * 1) the player performed it entirely without shifting the playback progress;
 * 2) the player performed one or several chunks of the song and in summary, the duration of these chunks is at least [MIN_VALUABLE_DURATION] milliseconds;
 * NOTE: If song's duration is less than [MIN_VALUABLE_DURATION] then it's enough to perform 90% of the song.
 */
class SongPlayCountObserver constructor(
    private val schedulerProvider: SchedulerProvider,
    private val dispatchSongPlayedUseCase: DispatchSongPlayedUseCase
): SimplePlayerObserver() {

    private companion object {
        const val NEGLIGIBLE_DURATION = 0.01f

        const val MIN_VALUABLE_DURATION = 5_000 // 5 seconds

        const val MIN_VALUABLE_PERCENT_OF_DURATION = 0.3f // 30% of duration

        const val PLAYBACK_NOT_STARTED_YET = -1L
    }

    private val disposables = CompositeDisposable()

    private var currentItem: AudioSource? = null
    private var currentItemDuration: Int = 0
    private var isPrepared: Boolean = false
    private var isPlaying: Boolean = false
    // Remembers the last timestamp when the playback started
    private var lastTimePlaybackStarted: Long = PLAYBACK_NOT_STARTED_YET
    // Total duration of playback performance
    private var performanceDuration: Int = 0

    private fun now(): Long = System.currentTimeMillis()

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        if (isPlaying) {
            // it was playing
            val performed = now() - lastTimePlaybackStarted
            performanceDuration += performed.toInt()
        }

        val isPerformed = when {
            // If it wasn't even prepared then do not consider it as played
            !isPrepared -> false

            // If audio's duration is so negligible then ignore it
            currentItemDuration < NEGLIGIBLE_DURATION -> false

            currentItemDuration < MIN_VALUABLE_DURATION -> {
                performanceDuration >= currentItemDuration * 0.9 // 90% of audio's duration
            }

            else -> {
                val percent = performanceDuration.toFloat() / currentItemDuration
                percent >= MIN_VALUABLE_PERCENT_OF_DURATION && performanceDuration >= MIN_VALUABLE_DURATION * 0.9
            }
        }

        if (isPerformed) {
            currentItem?.also { safeItem ->
                dispatchSongPlayedUseCase.dispatchSongPlayed(safeItem.toSong())
                        .observeOn(schedulerProvider.main())
                        .subscribe({ }, { })
                        .also { d -> disposables.add(d) }
            }
        }

        currentItem = item
        currentItemDuration = 0
        isPrepared = player.isPrepared()
        isPlaying = player.isPlaying()

        lastTimePlaybackStarted = if (!isPlaying) PLAYBACK_NOT_STARTED_YET else now()
        performanceDuration = 0
    }

    override fun onPrepared(player: Player) {
        isPrepared = true
        currentItemDuration = player.getDuration()
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