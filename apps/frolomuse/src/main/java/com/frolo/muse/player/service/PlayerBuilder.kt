package com.frolo.muse.player.service

import android.app.Service
import android.media.MediaPlayer
import android.os.Handler
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.WorkerThread
import com.frolo.audiofx2.impl.AudioFx2AttachTarget
import com.frolo.audiofx2.impl.AudioFx2Impl
import com.frolo.debug.DebugUtils
import com.frolo.muse.BuildInfo
import com.frolo.logger.api.Logger
import com.frolo.muse.player.service.observers.*
import com.frolo.muse.model.playback.PlaybackFadingParams
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.repository.SongRepository
import com.frolo.player.*
import com.frolo.threads.ThreadStrictMode
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject


class PlayerBuilder @Inject constructor(
    private val service: Service,
    private val audioFx2Impl: AudioFx2Impl,
    private val mediaSession: MediaSessionCompat,
    private val notificationSender: PlayerNotificationSender,
    private val playerJournal: PlayerJournal,
    private val preferences: Preferences,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val schedulerProvider: SchedulerProvider,
    private val songRepository: SongRepository
) {

    @WorkerThread
    fun build(): Player {
        ThreadStrictMode.assertBackground()
        val startTime = System.currentTimeMillis()
        val instance = buildImpl()
        val elapsedTime = System.currentTimeMillis() - startTime
        Logger.d(LOG_TAG, "Built player instance for $elapsedTime millis")
        return instance
    }

    private fun buildImpl(): Player {
        val preParams = loadPreParams(timeoutMillis = getTimeoutMillis())
        val player = PlayerImpl.newBuilder(service)
            .setDebug(BuildInfo.isDebug())
            .setMediaPlayerHook(MediaPlayerHookImpl(audioFx2Impl))
            .setPlayerJournal(playerJournal)
            .setUseWakeLocks(true)
            .setRepeatMode(preParams.repeatMode)
            .setShuffleMode(preParams.shuffleMode)
            .setPlaybackFadingStrategy(preParams.playbackFadingStrategy)
            .addObserver(InternalErrorHandler(service))
            .addObserver(PlayerStateSaver(preferences))
            .addObserver(SongPlayCounter(schedulerProvider, songRepository))
            .addObserver(WidgetUpdater(service))
            .addObserver(PlayerNotifier(service, songRepository, notificationSender))
            .build()

        MediaSessionObserver.attach(service, mediaSession, player)
        mediaSession.setCallback(MediaSessionCallbackImpl(player), Handler(service.mainLooper))

        return player
    }

    private fun getTimeoutMillis(): Long {
        return if (BuildInfo.isDebug()) {
            4000L
        } else {
            // Let's not torture users. This should not block the cold start.
            2000L
        }
    }

    private fun loadPreParams(timeoutMillis: Long): PreParams {
        val countDownLatch = CountDownLatch(2)

        val playbackFadingParamsRef = AtomicReference<PlaybackFadingParams>(null)

        val disposables = CompositeDisposable()

        preferences.playbackFadingParams
            .first(PlaybackFadingParams.none())
            .timeout(timeoutMillis, TimeUnit.MILLISECONDS)
            .doFinally { countDownLatch.countDown() }
            .subscribe(
                { params -> playbackFadingParamsRef.set(params) },
                { err -> logOrFail(err) }
            )
            .let(disposables::add)

        try {
            countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (err: Throwable) {
            logOrFail(err)
        }
        disposables.dispose()

        val playbackFadingParams = playbackFadingParamsRef.get() ?: PlaybackFadingParams.none()

        return PreParams(
            playbackFadingStrategy = PlaybackFadingStrategy
                .withSmartStaticInterval(playbackFadingParams.interval),
            repeatMode = preferences.loadRepeatMode(),
            shuffleMode = preferences.loadShuffleMode()
        )
    }

    private fun logOrFail(err: Throwable) {
        DebugUtils.dumpOnMainThread(err)
        Logger.e(LOG_TAG, err)
    }

    private class PreParams(
        val playbackFadingStrategy: PlaybackFadingStrategy,
        val repeatMode: Int,
        val shuffleMode: Int
    )

    private class MediaPlayerHookImpl(val audioFx2Impl: AudioFx2Impl): MediaPlayerHook {
        override fun attachAudioEffects(mediaPlayer: MediaPlayer) {
            val target = AudioFx2AttachTarget(
                priority = 0,
                sessionId = mediaPlayer.audioSessionId,
                mediaPlayer = mediaPlayer
            )
            audioFx2Impl.attachTo(target)
        }

        override fun releaseAudioEffects() {
            audioFx2Impl.release()
        }
    }

    private companion object {
        private const val LOG_TAG = "PlayerBuilder"
    }

}