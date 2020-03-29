package com.frolo.muse.engine.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.frolo.muse.App
import com.frolo.muse.R
import com.frolo.muse.Trace
import com.frolo.muse.engine.*
import com.frolo.muse.engine.audiofx.AudioFx_Impl
import com.frolo.muse.engine.service.PlayerService.Companion.newIntent
import com.frolo.muse.engine.service.PlayerService.PlayerBinder
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.interactor.media.DispatchSongPlayedUseCase
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.sleeptimer.PlayerSleepTimer
import com.frolo.muse.ui.main.MainActivity
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
class PlayerService: Service() {
    companion object {
        private const val TAG = "PlayerService"

        private const val RC_COMMAND_SKIP_TO_PREVIOUS = 151
        private const val RC_COMMAND_TOGGLE = 152
        private const val RC_COMMAND_SKIP_TO_NEXT = 153
        private const val RC_OPEN_PLAYER = 157
        private const val RC_CANCEL_NOTIFICATION = 159

        private const val EXTRA_COMMAND = "command"

        // commands
        const val COMMAND_EMPTY = 10
        const val COMMAND_SKIP_TO_PREVIOUS = 11
        const val COMMAND_SKIP_TO_NEXT = 12
        const val COMMAND_TOGGLE = 13
        const val COMMAND_SWITCH_TO_NEXT_REPEAT_MODE = 14
        const val COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE = 15
        const val COMMAND_STOP = 18
        const val COMMAND_CANCEL_NOTIFICATION = 19
        const val COMMAND_SHOW_NOTIFICATION = 20

        // notification
        @Deprecated("Use CHANNEL_ID_PLAYBACK instead")
        private const val CHANNEL_ID_PLAYBACK_OLD = "audio_playback"
        private const val CHANNEL_ID_PLAYBACK = "playback"
        private const val NOTIFICATION_ID_PLAYBACK = 1001

        fun newIntent(context: Context): Intent = Intent(context, PlayerService::class.java)

        fun newIntent(context: Context, command: Int): Intent = Intent(context, PlayerService::class.java)
                .putExtra(EXTRA_COMMAND, command)

        private fun getCommandExtra(intent: Intent) = intent.getIntExtra(EXTRA_COMMAND, COMMAND_EMPTY)
    }

    class PlayerBinder constructor(val service: Player): Binder()

    override fun onBind(intent: Intent?): IBinder {
        isBound = true
        Trace.d(TAG, "Service gets bound")
        return PlayerBinder(player)
    }

    override fun onRebind(intent: Intent?) {
        isBound = true
        Trace.d(TAG, "Service gets rebound")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        Trace.d(TAG, "Service gets unbound")
        if (notificationCancelled) {
            // Service is unbound and not in foreground. We don't know why it got unbound.
            // The user may have closed the app or the system killed activities due to low memory. Who knows.
            // Give it the last chance to be alive? NO!

            Trace.w(TAG, "Service is not in foreground. STOP IT!")
            // The service is not in foreground. It may live only 60 seconds if it's Android API v26+
            // There is no sense to continue running at all: no bound clients and no notification.
            // Then STOP IT.
            stopSelf()
        }
        // return true to get onRebind called later
        return true
    }

    private val serviceName = "com.frolo.muse.engine.service.PlayerService"
    private var isBound = false // indicates whether the service is bound or not
    private var notificationCancelled = false

    private lateinit var player: Player

    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var presetRepository: PresetRepository
    @Inject
    lateinit var schedulerProvider: SchedulerProvider
    @Inject
    lateinit var dispatchSongPlayedUseCase: DispatchSongPlayedUseCase

    // You may be interested, why we use broadcast receiver (PendingIntent.getBroadcast) instead of simply starting service (PendingIntent.getService).
    // Well, this receiver is registered only while the service is running, so it will not receive any intent if the service is destroyed.
    // On the other hand, if we use PendingIntent.getService, the service will be started even the app is not running.
    private val sleepTimerHandler = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null && intent.action == PlayerSleepTimer.ACTION_ALARM_TRIGGERED) {
                // Need to reset the current sleep timer because its pending intent is still retained,
                // therefore the app settings may think that an alarm is still set.
                PlayerSleepTimer.resetCurrentSleepTimer(context)
                Trace.d(TAG, "Received sleep timer broadcast message. Pausing playback")
                player.pause()
            }
        }
    }

    // Handling headset plug-state callbacks
    private val headsetPlugHandler = object : HeadsetPlugHandler() {
        override fun onHeadsetUnplugged(context: Context) {
            if (preferences.shouldPauseOnUnplugged()) {
                player.pause()
            }
        }
        override fun onHeadsetPlugged(context: Context) {
            if (preferences.shouldResumeOnPluggedIn()) {
                player.start()
            }
        }
    }

    // Handling headset button actions
    private lateinit var mediaSession: MediaSessionCompat
    private val mediaSessionCallback = object : MediaSessionCallback() {
        override fun onTogglePlayback() = player.toggle()
        override fun onSkipToNext() = player.skipToNext()
        override fun onSkipToPrevious() = player.skipToPrevious()
    }

    /**
     * Initializing all resources here
     */
    override fun onCreate() {
        super.onCreate()
        (application as App).appComponent.inject(this)

        // This is the first what we have to do.
        // Why? Because initialization may take some time.
        // And I want to be sure that it will not crash due to the Foreground service policy.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Creating notification channel
            createPlaybackNotificationChannel()
            postPromiseNotification()
        }

        // Creating engine and ui handlers
        val thread = HandlerThread("Service[$serviceName]", Process.THREAD_PRIORITY_URGENT_AUDIO)
        thread.start()

        // Handlers
        val engineHandler = Handler(thread.looper)
        val eventHandler = Handler(Looper.getMainLooper())

        // Creating eq impl
        val audioFxApplicable: AudioFxApplicable =
                AudioFx_Impl.getInstance(this, "com.frolo.muse.audiofx.persistence")

        val observerRegistry = ObserverRegistry(this) { player, forceNotify ->
            notifyAboutPlayback(forceNotify)
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        player = PlayerEngine(engineHandler, eventHandler, audioFxApplicable, observerRegistry, audioManager)

        registerReceiver(headsetPlugHandler, IntentFilter(Intent.ACTION_HEADSET_PLUG))
        registerReceiver(sleepTimerHandler, IntentFilter(PlayerSleepTimer.ACTION_ALARM_TRIGGERED))

        mediaSession = MediaSessionCompat(applicationContext, packageName).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
            isActive = true
            setCallback(mediaSessionCallback)
        }

        // setting up modes after preferences initialized
        preferences.apply {
            player.setRepeatMode(loadRepeatMode())
            player.setShuffleMode(loadShuffleMode())
        }

        player.registerObserver(PlayerStateObserver(preferences))
        player.registerObserver(SongPlayCountObserver(schedulerProvider, dispatchSongPlayedUseCase))

        Trace.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent != null) {
            when(getCommandExtra(intent)) {
                COMMAND_SKIP_TO_PREVIOUS -> player.skipToPrevious()

                COMMAND_SKIP_TO_NEXT -> player.skipToNext()

                COMMAND_TOGGLE -> player.toggle()

                COMMAND_SWITCH_TO_NEXT_REPEAT_MODE ->
                    player.switchToNextRepeatMode()

                COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE ->
                    player.switchToNextShuffleMode()

                COMMAND_CANCEL_NOTIFICATION -> cancelNotification()

                COMMAND_STOP -> player.pause()

                COMMAND_SHOW_NOTIFICATION -> showNotification()
            }
            START_STICKY
        } else START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Trace.d(TAG, "Task removed!")
    }

    override fun onDestroy() {
        Trace.d(TAG, "Service died. Cleaning callbacks")

        // notifying observers that player is shutting down and removing them all
        player.shutdown()

        unregisterReceiver(headsetPlugHandler)
        unregisterReceiver(sleepTimerHandler)

        mediaSession.release()

        super.onDestroy()
    }

    /********************************
     ********* NOTIFICATION *********
     *******************************/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPlaybackNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        if (manager != null) {
            Trace.d(TAG, "Deleting old notification channel for playback")
            manager.deleteNotificationChannel(CHANNEL_ID_PLAYBACK_OLD)

            Trace.d(TAG, "Creating notification channel for playback")
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
        Trace.d(TAG, "Notification cancelled. Stopping foreground")
        stopForeground(true)
        if (isBound.not()) {
            Trace.w(TAG, "No bound clients. STOP IT!")
            // No clients bound to the service. It may live only 60 seconds if it's Android API v26+
            // It makes no sense to continue running at all: no bound clients and no notification.
            // Then STOP IT.
            stopSelf()
        }
    }

    private fun showNotification() {
        Trace.d(TAG, "Showing notification")
        notificationCancelled = false
        notifyAboutPlayback(false)
    }

    private fun buildPromiseNotification(): Notification {
        return buildPlaybackNotification(null, false)
    }

    private fun buildPlaybackNotification(song: Song?, isPlaying: Boolean): Notification {
        // building notification
        val remoteViews = RemoteViews(packageName, R.layout.notification_playback).apply {
            setTextViewText(R.id.tv_song_name, song?.title ?: "")
            setTextViewText(R.id.tv_artist_name, song?.artist ?: "")
            setImageViewResource(R.id.btn_play, if (isPlaying) R.drawable.ic_cpause else R.drawable.ic_play)

            val context = this@PlayerService

            newIntent(context, COMMAND_CANCEL_NOTIFICATION).also { intent ->
                setOnClickPendingIntent(R.id.btn_close,
                        PendingIntent.getService(
                                context,
                                RC_CANCEL_NOTIFICATION,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }

            newIntent(context, COMMAND_SKIP_TO_PREVIOUS).also { intent ->
                setOnClickPendingIntent(R.id.btn_skip_to_previous,
                        PendingIntent.getService(
                                context,
                                RC_COMMAND_SKIP_TO_PREVIOUS,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }

            newIntent(context, COMMAND_TOGGLE).also { intent ->
                setOnClickPendingIntent(R.id.btn_play,
                        PendingIntent.getService(
                                context,
                                RC_COMMAND_TOGGLE,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }

            newIntent(context, COMMAND_SKIP_TO_NEXT).also { intent ->
                setOnClickPendingIntent(R.id.btn_skip_to_next,
                        PendingIntent.getService(
                                context,
                                RC_COMMAND_SKIP_TO_NEXT,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }

            MainActivity.newIntent(context, MainActivity.INDEX_LIBRARY).also { intent ->
                setOnClickPendingIntent(R.id.ll_root,
                        PendingIntent.getActivity(
                                context,
                                RC_OPEN_PLAYER,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT))
            }
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_PLAYBACK)
                .setSmallIcon(R.drawable.ic_app_brand)
                //.setAutoCancel(false)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

        //region Load image
        val target = SafeNotificationTarget.create(
                this, R.id.imv_album_art, remoteViews, notification, NOTIFICATION_ID_PLAYBACK)
        val error = Glide.with(this)
                .asBitmap()
                .load(R.drawable.png_note_256x256)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        val options = GlideAlbumArtHelper.get()
                .makeRequestOptions(song?.albumId ?: -1)
                .placeholder(R.drawable.png_note_256x256)
                .error(R.drawable.png_note_256x256)
        val uri = GlideAlbumArtHelper.getUri(song?.albumId ?: -1)
        Glide.with(this)
                .asBitmap()
                .load(uri)
                .apply(options)
                .error(error)
                .into(target)
        //endregion

        return notification
    }

    //@MainThread
    private fun notifyAboutPlayback(forceNotify: Boolean) {
        if (notificationCancelled && !forceNotify) {
            return
        }

        val song = player.getCurrent()
        val isPlaying = player.isPlaying()

        val notification = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // It's necessary to wrap building notification in try-catch for SDK version <= LOLLIPOP_MR1.
            // See https://fabric.io/frolovs-projects/android/apps/com.frolo.musp/issues/52dcccd58785f4ffa06d11c944cc982c
            try {
                buildPlaybackNotification(song, isPlaying)
            } catch (e: Throwable) {
                Trace.e(TAG, e)
                null
            }
        } else buildPlaybackNotification(song, isPlaying)

        if (notification == null) {
            Trace.w(TAG, "Failed to build notification.")
            return
        }

        // We're about to post the notification. It's not cancelled now
        notificationCancelled = false

        Trace.d(TAG, "Starting foreground by posting notification")
        startForeground(NOTIFICATION_ID_PLAYBACK, notification)
    }
}