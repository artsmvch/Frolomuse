package com.frolo.muse.engine.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.frolo.audiofx.applicable.AudioFxApplicable
import com.frolo.muse.BuildConfig
import com.frolo.muse.Logger
import com.frolo.muse.R
import com.frolo.muse.common.*
import com.frolo.muse.engine.*
import com.frolo.audiofx.AudioFxImpl
import com.frolo.muse.engine.service.PlayerService.Companion.newIntent
import com.frolo.muse.engine.service.PlayerService.PlayerBinder
import com.frolo.muse.engine.service.audiofx.DefaultAudioFxErrorHandler
import com.frolo.muse.engine.service.observers.*
import com.frolo.headset.createHeadsetHandler
import com.frolo.muse.di.appComponent
import com.frolo.muse.interactor.media.DispatchSongPlayedUseCase
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
import com.frolo.muse.interactor.player.RestorePlayerStateUseCase
import com.frolo.music.model.Song
import com.frolo.muse.model.playback.PlaybackFadingParams
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.repository.RemoteConfigRepository
import com.frolo.muse.rx.RxService
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.sleeptimer.PlayerSleepTimer
import com.frolo.muse.ui.main.MainActivity
import com.frolo.player.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * The heart of the app. It must be running as long as the app is alive.
 *
 * Here is the whole logic of playing, navigating and dispatching events.
 *
 * Communication with the service can be through binding to [PlayerBinder] or intents (see [newIntent]]).
 *
 * A little tutorial for this service.
 * When the service creates, it starts foreground by posting a notification;
 * The notification may be cancelled by user, it this case case the server stops foreground and STOPS ITSELF.
 * (!) If so and the service is NOT bound then the service will be destroyed.
 * Otherwise, if the service is bound, then it keeps working as if nothing has changed.
 * (!) If the service gets unbound and the notification was cancelled before, then it will be destroyed.
 * Thus the service may be in the following conditions:
 * 1) Not foreground and not bound => will be destroyed by the system asap (~60 seconds);
 * 2) Foreground but unbound => will keep working;
 * 3) Not foreground but bound => will keep working;
 * 4) Foreground and bound => will keep working;
 * So to be working as long as needed, the service must be at least foreground or bound.
 * See https://stackoverflow.com/a/17883828/9437681
 */
class PlayerService: RxService(), PlayerNotificationSender {

    class PlayerBinder constructor(val service: Player): Binder()

    private var isBound = false // indicates whether the service is bound or not
    private var notificationCancelled = false

    private lateinit var player: Player

    @Inject
    lateinit var playerJournal: PlayerJournal
    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var presetRepository: PresetRepository
    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var dispatchSongPlayedUseCase: DispatchSongPlayedUseCase
    @Inject
    lateinit var getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>
    @Inject
    lateinit var changeFavouriteUseCase: ChangeFavouriteUseCase<Song>
    @Inject
    lateinit var restorePlayerStateUseCase: RestorePlayerStateUseCase

    // HeadsetHandler is used to handle the status of the headset (connected, disconnected, etc.)
    private val headsetHandler = createHeadsetHandler(
        onConnected = {
            if (preferences.shouldResumeOnPluggedIn()) {
                player.start()
            }
        },
        onDisconnected = {
            if (preferences.shouldPauseOnUnplugged()) {
                player.pause()
            }
        },
        onBecomeWeird = {
            // no actions
        }
    )

    // Handler for Sleep Timer
    private val sleepTimerHandler = PlayerSleepTimer.createBroadcastReceiver {
        Logger.d(TAG, "Sleep Timer triggered: pausing the playback")
        player.pause()
    }

    // MediaSession is used to control buttons clicks from headsets and playback notifications.
    private lateinit var mediaSession: MediaSessionCompat
    private val mediaSessionCallback = object : MediaSessionCallback() {
        override fun onTogglePlayback() {
            player.toggle()
        }

        override fun onSkipToNext() {
            player.skipToNext()
        }

        override fun onSkipToPrevious() {
            player.skipToPrevious()
        }

        override fun onSeekTo(pos: Long) {
            player.seekTo(pos.toInt())
        }
    }

    //region Service binding

    override fun onBind(intent: Intent?): IBinder {
        isBound = true
        Logger.d(TAG, "Service gets bound")
        return PlayerBinder(player)
    }

    override fun onRebind(intent: Intent?) {
        isBound = true
        Logger.d(TAG, "Service gets rebound")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        Logger.d(TAG, "Service gets unbound")
        if (notificationCancelled) {
            // Service is unbound and not in foreground. We don't know why it got unbound.
            // The user may have closed the app or the system killed activities due to low memory. Who knows.
            // Give it the last chance to be alive? NO!

            Logger.w(TAG, "Service is not in foreground. STOP IT!")
            // The service is not in foreground. It may live only 60 seconds if it's Android API v26+
            // There is no sense to continue running at all: no bound clients and no notification.
            // Then STOP IT.
            stopSelf()
        }
        // return true to get onRebind called later
        return true
    }

    //endregion

    /**
     * Initializing all resources here
     */
    override fun onCreate() {
        super.onCreate()

        // First, inject dependencies
        appComponent.inject(this)

        // Setting up media session
        mediaSession = MediaSessionCompat(applicationContext, "frolomuse:player_service").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
            isActive = true
            setCallback(mediaSessionCallback)
            setEmptyMetadata()
        }

        // The promise notification must be posted asap.
        // Why? Because initialization may take some time.
        // And I want to be sure that it will not crash due to the Foreground service policy.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Creating notification channel
            createPlaybackNotificationChannel()
            postPromiseNotification()
        }

        // Creating AudioFx
        val audioFx: AudioFxApplicable = AudioFxImpl.getInstance(
                this, Const.AUDIO_FX_PREFERENCES, DefaultAudioFxErrorHandler())

        // Building player instance
        player = PlayerImpl.newBuilder(this, audioFx)
            .setDebug(BuildConfig.DEBUG)
            .setPlayerJournal(playerJournal)
            .setUseWakeLocks(quicklyGetIsPlayerWakeLockEnabled())
            // Setting up repeat and shuffle modes
            .setRepeatMode(preferences.loadRepeatMode())
            .setShuffleMode(preferences.loadShuffleMode())
            // Setting up playback fading strategy
            .setPlaybackFadingStrategy(quicklyRestorePlaybackFadingStrategy())
            // Adding all the necessary observers
            .addObserver(InternalErrorHandler(this))
            .addObserver(PlayerStateSaver(preferences))
            .addObserver(SongPlayCounter(schedulerProvider, dispatchSongPlayedUseCase))
            .addObserver(WidgetUpdater(this))
            .addObserver(PlayerNotifier(this, getIsFavouriteUseCase, this))
            .build()

        // Attaching media session observer
        MediaSessionObserver.attach(this, mediaSession, player)

        // Subscribing on the headset status changes
        headsetHandler.subscribe(this)

        // Subscribing on the Sleep Timer
        registerReceiver(sleepTimerHandler, PlayerSleepTimer.createIntentFilter())

        Logger.d(TAG, "Service created")
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
            if (DEBUG) throw err
            PlaybackFadingParams.none()
        }
        return PlaybackFadingStrategy.withSmartStaticInterval(params.interval)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) return START_STICKY

        val calledFromWidget = intent.getBooleanExtra(EXTRA_CALLED_FROM_WIDGET, false)

        // Checking which command was the given to the player
        when (intent.getIntExtra(EXTRA_COMMAND, COMMAND_EMPTY)) {
            COMMAND_SKIP_TO_PREVIOUS -> {
                player.performIntentAction(calledFromWidget) { skipToPrevious() }
            }

            COMMAND_SKIP_TO_NEXT -> {
                player.performIntentAction(calledFromWidget) { skipToNext() }
            }

            COMMAND_TOGGLE -> {
                player.performIntentAction(calledFromWidget) { toggle() }
            }

            COMMAND_SWITCH_TO_NEXT_REPEAT_MODE -> {
                player.performIntentAction(calledFromWidget) { switchToNextRepeatMode() }
            }

            COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE -> {
                player.performIntentAction(calledFromWidget) { switchToNextShuffleMode() }
            }

            COMMAND_CANCEL_NOTIFICATION -> cancelNotification()

            COMMAND_CHANGE_FAV -> {
                (intent.getSerializableExtra(EXTRA_SONG) as? Song)?.also { safeSong ->
                    changeFavouriteUseCase.changeFavourite(safeSong).subscribeSafely()
                }
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Logger.d(TAG, "Task removed!")
    }

    override fun onDestroy() {
        Logger.d(TAG, "Service died. Cleaning callbacks")

        // Shutting down the player and disposing all the resources.
        // The player shutdown call should clear its observers by itself.

        player.shutdown()

        mediaSession.release()

        headsetHandler.dispose()

        unregisterReceiver(sleepTimerHandler)

        super.onDestroy()
    }

    private fun Player.performIntentAction(requireNonEmptyQueue: Boolean, action: Player.() -> Unit) {
        val queue = getCurrentQueue()
        if (requireNonEmptyQueue && queue.isNullOrEmpty()) {
            restorePlayerStateUseCase
                .restorePlayerStateIfNeeded(this)
                .doOnComplete { this.action() }
                .subscribeSafely()
        } else {
            this.action()
        }
    }

    /********************************
     ********* NOTIFICATION *********
     *******************************/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPlaybackNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        if (manager != null) {
            Logger.d(TAG, "Deleting old notification channel for playback")
            manager.deleteNotificationChannel(CHANNEL_ID_PLAYBACK_OLD)

            Logger.d(TAG, "Creating notification channel for playback")
            val channelName = getString(R.string.playback_channel_name)
            val channelDesc = getString(R.string.playback_channel_desc)
            val channel = NotificationChannel(
                    CHANNEL_ID_PLAYBACK,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
                description = channelDesc
            }
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postPromiseNotification() {
        startForeground(NOTIFICATION_ID_PLAYBACK, buildPromiseNotification())
    }

    private fun cancelNotification() {
        notificationCancelled = true
        player.pause()
        Logger.d(TAG, "Notification cancelled. Stopping foreground")
        stopForeground(true)
        if (isBound.not()) {
            Logger.w(TAG, "No bound clients. STOP IT!")
            // No clients bound to the service. It may live only 60 seconds if it's Android API v26+
            // It makes no sense to continue running at all: no bound clients and no notification.
            // Then STOP IT.
            stopSelf()
        }
    }

    private fun buildPromiseNotification(): Notification {
        return buildPlayerNotification(PlayerNotificationParams.NONE)
    }

    private fun buildPlayerNotification(params: PlayerNotificationParams): Notification {

        val item = params.item
        val art = params.art
        val isPlaying = params.isPlaying
        val isFav = params.isFavourite

        val context = this@PlayerService

        val cancelPendingIntent = newIntent(context, COMMAND_CANCEL_NOTIFICATION).let { intent ->
            PendingIntent.getService(context, RC_CANCEL_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val changeFavPendingIntent = newIntent(context, COMMAND_CHANGE_FAV).let { intent ->
            intent.putExtra(EXTRA_SONG, item?.toSong())
            PendingIntent.getService(context, RC_CHANGE_FAV, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val previousPendingIntent = newIntent(context, COMMAND_SKIP_TO_PREVIOUS).let { intent ->
            PendingIntent.getService(context, RC_COMMAND_SKIP_TO_PREVIOUS, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val togglePendingIntent = newIntent(context, COMMAND_TOGGLE).let { intent ->
            PendingIntent.getService(context, RC_COMMAND_TOGGLE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val nextPendingIntent = newIntent(context, COMMAND_SKIP_TO_NEXT).let { intent ->
            PendingIntent.getService(context, RC_COMMAND_SKIP_TO_NEXT, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val openPendingIntent = MainActivity.newIntent(context = context, openPlayer = true).let { intent ->
            PendingIntent.getActivity(context, RC_OPEN_PLAYER, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID_PLAYBACK).apply {
            setSmallIcon(R.drawable.ic_player_notification_small)

            setContentTitle(item?.title.orEmpty())
            setContentText(item?.artist.orEmpty())

            setContentIntent(openPendingIntent)

            setPriority(NotificationCompat.PRIORITY_LOW)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        if (MODERN_PLAYBACK_NOTIFICATION) {

            notificationBuilder.apply {
                addAction(if (isFav) R.drawable.ntf_ic_liked else R.drawable.ntf_ic_not_liked, "Change_Fav", changeFavPendingIntent)
                addAction(R.drawable.ntf_ic_previous, "Previous", previousPendingIntent)
                if (isPlaying) {
                    addAction(R.drawable.ntf_ic_pause, "Pause", togglePendingIntent)
                } else {
                    addAction(R.drawable.ntf_ic_play, "Play", togglePendingIntent)
                }
                addAction(R.drawable.ntf_ic_next, "Next", nextPendingIntent)
                addAction(R.drawable.ntf_ic_cancel, "Cancel", cancelPendingIntent)

                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1, 2, 3)
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowCancelButton(false)
                )

                setLargeIcon(art)
            }

        } else {

            val remoteViews = RemoteViews(context.packageName, R.layout.notification_playback).apply {
                setTextViewText(R.id.tv_song_name, item?.title ?: "")
                setTextViewText(R.id.tv_artist_name, item?.artist ?: "")
                setImageViewResource(R.id.btn_play, if (isPlaying) R.drawable.ic_pause_18dp else R.drawable.ic_play_18dp)
                setImageViewBitmap(R.id.imv_album_art, art)

                setOnClickPendingIntent(R.id.btn_cancel, cancelPendingIntent)
                setOnClickPendingIntent(R.id.btn_skip_to_previous, previousPendingIntent)
                setOnClickPendingIntent(R.id.btn_play, togglePendingIntent)
                setOnClickPendingIntent(R.id.btn_skip_to_next, nextPendingIntent)
                setOnClickPendingIntent(R.id.root_container, openPendingIntent)
            }

            notificationBuilder.apply {
                //setStyle(NotificationCompat.DecoratedCustomViewStyle())
                //setCustomContentView(remoteViews)
                setCustomBigContentView(remoteViews)
                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                )
            }

        }

        return notificationBuilder.build()
    }

    override fun sendPlayerNotification(params: PlayerNotificationParams, forced: Boolean) {
        if (notificationCancelled && !forced) {
            return
        }

        val notification = buildPlayerNotification(params)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID_PLAYBACK, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID_PLAYBACK, notification)
        }

        // We're about to post the notification. It's not cancelled now
        notificationCancelled = false
    }

    companion object {

        private val DEBUG = BuildConfig.DEBUG

        private const val TAG = "PlayerService"

        private const val RC_COMMAND_SKIP_TO_PREVIOUS = 151
        private const val RC_COMMAND_TOGGLE = 152
        private const val RC_COMMAND_SKIP_TO_NEXT = 153
        private const val RC_CHANGE_FAV = 155
        private const val RC_OPEN_PLAYER = 157
        private const val RC_CANCEL_NOTIFICATION = 159

        private const val EXTRA_COMMAND = "command"
        private const val EXTRA_SONG = "song"
        private const val EXTRA_CALLED_FROM_WIDGET = "called_from_widget"

        // commands
        const val COMMAND_EMPTY = 10
        const val COMMAND_SKIP_TO_PREVIOUS = 11
        const val COMMAND_SKIP_TO_NEXT = 12
        const val COMMAND_TOGGLE = 13
        const val COMMAND_SWITCH_TO_NEXT_REPEAT_MODE = 14
        const val COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE = 15
        const val COMMAND_CHANGE_FAV = 17
        const val COMMAND_CANCEL_NOTIFICATION = 19

        // notification
        @Deprecated("Use CHANNEL_ID_PLAYBACK instead")
        private const val CHANNEL_ID_PLAYBACK_OLD = "audio_playback"
        private const val CHANNEL_ID_PLAYBACK = "playback"
        private const val NOTIFICATION_ID_PLAYBACK = 1001

        // For now, we'd better use the modern playback notification,
        // because it is correctly shown on the lock screen.
        // Our custom notification has some issues and is not shown on the lock screen for some devices.
        private val MODERN_PLAYBACK_NOTIFICATION = true //Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        /**
         * Starts the PlayerService in foreground.
         */
        @JvmStatic
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, newIntent(context))
        }

        @JvmStatic
        fun newIntent(context: Context): Intent = Intent(context, PlayerService::class.java)

        @JvmStatic
        fun newIntent(context: Context, command: Int): Intent = Intent(context, PlayerService::class.java)
                .putExtra(EXTRA_COMMAND, command)

        /**
         * A variant for [newIntent], which specifies that the intent will be called from the app widget.
         */
        @JvmStatic
        fun newIntentFromWidget(context: Context, command: Int): Intent = Intent(context, PlayerService::class.java)
                .putExtra(EXTRA_COMMAND, command)
                .putExtra(EXTRA_CALLED_FROM_WIDGET, true)

    }

}