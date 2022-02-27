package com.frolo.muse.engine.service

import android.app.Service
import android.support.v4.media.session.MediaSessionCompat
import com.frolo.audiofx.AudioFxImpl
import com.frolo.audiofx.applicable.AudioFxApplicable
import com.frolo.debug.DebugUtils
import com.frolo.muse.BuildConfig
import com.frolo.muse.Logger
import com.frolo.muse.engine.service.audiofx.DefaultAudioFxErrorHandler
import com.frolo.muse.engine.service.observers.*
import com.frolo.muse.model.playback.PlaybackFadingParams
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.repository.SongRepository
import com.frolo.player.PlaybackFadingStrategy
import com.frolo.player.Player
import com.frolo.player.PlayerImpl
import com.frolo.player.PlayerJournal
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PlayerBuilder @Inject constructor(
    private val service: Service,
    private val mediaSession: MediaSessionCompat,
    private val notificationSender: PlayerNotificationSender,
    private val playerJournal: PlayerJournal,
    private val preferences: Preferences,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val schedulerProvider: SchedulerProvider,
    private val songRepository: SongRepository
) {

    fun build(): Player {
        // Creating AudioFx
        val audioFx: AudioFxApplicable = AudioFxImpl.getInstance(
            service, Const.AUDIO_FX_PREFERENCES, DefaultAudioFxErrorHandler())

        val player = PlayerImpl.newBuilder(service, audioFx)
            .setDebug(BuildConfig.DEBUG)
            .setPlayerJournal(playerJournal)
            .setUseWakeLocks(quicklyGetIsPlayerWakeLockEnabled())
            // Setting up repeat and shuffle modes
            .setRepeatMode(preferences.loadRepeatMode())
            .setShuffleMode(preferences.loadShuffleMode())
            // Setting up playback fading strategy
            .setPlaybackFadingStrategy(quicklyRestorePlaybackFadingStrategy())
            // Adding all the necessary observers
            .addObserver(InternalErrorHandler(service))
            .addObserver(PlayerStateSaver(preferences))
            .addObserver(SongPlayCounter(schedulerProvider, songRepository))
            .addObserver(WidgetUpdater(service))
            .addObserver(PlayerNotifier(service, songRepository, notificationSender))
            .build()

        MediaSessionObserver.attach(service, mediaSession, player)

        return player
    }

    private fun quicklyGetIsPlayerWakeLockEnabled(): Boolean {
        return try {
            remoteConfigRepository.isPlayerWakeLockEnabled()
                .timeout(1, TimeUnit.SECONDS)
                .blockingGet()
        } catch (err: Throwable) {
            Logger.e(err)
            false
        }
    }

    private fun quicklyRestorePlaybackFadingStrategy(): PlaybackFadingStrategy {
        // Safely restoring the playback fading
        val params: PlaybackFadingParams = try {
            preferences.playbackFadingParams
                .first(PlaybackFadingParams.none())
                .timeout(3, TimeUnit.SECONDS)
                .blockingGet()
        } catch (err: Throwable) {
            Logger.e(err)
            DebugUtils.dumpOnMainThread(err)
            PlaybackFadingParams.none()
        }
        return PlaybackFadingStrategy.withSmartStaticInterval(params.interval)
    }

}