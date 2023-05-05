package com.frolo.muse.player.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.frolo.headset.createHeadsetHandler
import com.frolo.logger.api.Logger
import com.frolo.muse.R
import com.frolo.muse.common.*
import com.frolo.muse.di.ServiceComponent
import com.frolo.muse.di.applicationComponent
import com.frolo.muse.di.modules.ServiceModule
import com.frolo.muse.player.PlayerHolder
import com.frolo.muse.player.PlayerStateRestorer
import com.frolo.muse.interactor.media.favourite.ChangeSongFavStatusUseCase
import com.frolo.muse.repository.Preferences
import com.frolo.muse.sleeptimer.PlayerSleepTimer
import com.frolo.muse.ui.main.MainActivity
import com.frolo.music.model.Song
import com.frolo.player.Player
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


internal class PlayerServiceDelegate(
    private val service: Service
): PlayerNotificationSender {

    private val internalDisposables = CompositeDisposable()

    private var isBound = false // indicates whether the service is bound or not
    private var isNotificationCancelled = false

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var serviceComponent: ServiceComponent

    private lateinit var playerBuilder: PlayerBuilder
    private lateinit var preferences: Preferences
    private lateinit var changeSongFavStatusUseCase: ChangeSongFavStatusUseCase
    private lateinit var playerStateRestorer: PlayerStateRestorer

    private val playerSubject: BehaviorSubject<Player> = BehaviorSubject.create()
    private var playerBuilderDisposable: Disposable? = null
    private val playerInstance: Player? get() = playerSubject.value

    // HeadsetHandler is used to handle the status of the headset (connected, disconnected, etc.)
    private val headsetHandler = createHeadsetHandler(
        onConnected = {
            if (preferences.shouldResumeOnPluggedIn()) {
                playerInstance?.start()
            }
        },
        onDisconnected = {
            if (preferences.shouldPauseOnUnplugged()) {
                playerInstance?.pause()
            }
        },
        onBecomeWeird = {
            // no actions
        }
    )

    // Handler for Sleep Timer
    private val sleepTimerHandler = PlayerSleepTimer.createBroadcastReceiver {
        Logger.d(TAG, "Sleep Timer triggered: pausing the playback")
        playerInstance?.pause()
    }

    fun onBind(intent: Intent?): IBinder {
        isBound = true
        Logger.d(TAG, "Service gets bound")
        return PlayerBinderImpl(playerSubject)
    }

    fun onRebind(intent: Intent?) {
        isBound = true
        Logger.d(TAG, "Service gets rebound")
    }

    fun onUnbind(intent: Intent?): Boolean {
        isBound = false
        Logger.d(TAG, "Service gets unbound")
        if (isNotificationCancelled) {
            // Service is unbound and not in foreground. We don't know why it got unbound.
            // The user may have closed the app or the system killed activities due to
            // low memory. Who knows. Give it the last chance to be alive? NO!
            Logger.w(TAG, "Service is not in foreground. STOP IT!")
            // The service is not in foreground. It may live only 60 seconds if it's Android API v26+
            // There is no sense to continue running at all: no bound clients and no notification.
            // Then STOP IT.
            service.stopSelf()
        }
        // return true to get onRebind called later
        return true
    }

    fun onCreate() {
        serviceComponent = buildServiceComponent()
        injectDependencies()
        // Android 8.1 requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createPlaybackNotificationChannel()
            sendPrimaryNotification()
        }
        buildPlayerInstanceAsync()
        headsetHandler.subscribe(service)
        service.registerReceiver(sleepTimerHandler, PlayerSleepTimer.createIntentFilter())
        Logger.d(TAG, "Service created")
    }

    private fun buildServiceComponent(): ServiceComponent {
        val serviceModule = ServiceModule(
            service = service,
            notificationSender = this
        )
        val applicationComponent = service.applicationComponent
        return applicationComponent.serviceComponent(serviceModule)
    }

    private fun injectDependencies() {
        mediaSession = serviceComponent.provideMediaSession()
        playerBuilder = serviceComponent.providePlayerBuilder()
        preferences = serviceComponent.providePreferences()
        changeSongFavStatusUseCase = serviceComponent.provideChangeSongFavStatusUseCase()
        playerStateRestorer = serviceComponent.providePlayerStateRestorer()
    }

    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return Service.START_STICKY
        val isWidgetCall = intent.getBooleanExtra(EXTRA_IS_WIDGET_CALL, false)
        when (intent.getIntExtra(EXTRA_CMD, PlayerServiceCmd.CMD_NO_OP)) {
            PlayerServiceCmd.CMD_SKIP_TO_PREVIOUS -> {
                runOnPlayer(isWidgetCall, Player::skipToPrevious)
            }
            PlayerServiceCmd.CMD_SKIP_TO_NEXT -> {
                runOnPlayer(isWidgetCall, Player::skipToNext)
            }
            PlayerServiceCmd.CMD_TOGGLE -> {
                runOnPlayer(isWidgetCall, Player::toggle)
            }
            PlayerServiceCmd.CMD_CHANGE_REPEAT_MODE -> {
                runOnPlayer(isWidgetCall, Player::switchToNextRepeatMode)
            }
            PlayerServiceCmd.CMD_CHANGE_SHUFFLE_MODE -> {
                runOnPlayer(isWidgetCall, Player::switchToNextShuffleMode)
            }
            PlayerServiceCmd.CMD_CANCEL_NOTIFICATION -> {
                cancelNotification()
            }
            PlayerServiceCmd.CMD_CHANGE_FAV_STATUS -> {
                (intent.getSerializableExtra(EXTRA_SONG) as? Song)?.also(::changeSongFavStatus)
            }
        }

        return Service.START_STICKY
    }

    fun onTaskRemoved(rootIntent: Intent?) {
        Logger.d(TAG, "Task removed!")
    }

    fun onDestroy() {
        Logger.d(TAG, "Service died. Cleaning callbacks")
        // The player shutdown call should clear its observers by itself.
        playerSubject.onComplete()
        disposePlayerBuilder()
        playerInstance?.shutdown()
        mediaSession.release()
        headsetHandler.dispose()
        service.unregisterReceiver(sleepTimerHandler)
        internalDisposables.dispose()
    }

    private fun buildPlayerInstanceAsync() {
        Single.fromCallable { playerBuilder.build() }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { playerInstance ->
                playerSubject.onNext(playerInstance)
            }
            .also { disposable ->
                disposePlayerBuilder()
                playerBuilderDisposable = disposable
            }
    }

    private fun disposePlayerBuilder() {
        playerBuilderDisposable?.dispose()
        playerBuilderDisposable = null
    }

    private inline fun runOnPlayer(
        ensureNonEmptyQueue: Boolean,
        crossinline action: Player.() -> Unit
    ) {
        playerSubject.firstElement()
            .flatMap { playerInstance ->
                val queue = playerInstance.getCurrentQueue()
                if (ensureNonEmptyQueue && queue.isNullOrEmpty()) {
                    playerStateRestorer.restorePlayerStateIfNeeded(playerInstance)
                        .andThen(Maybe.just(playerInstance))
                } else {
                    Maybe.just(playerInstance)
                }
            }
            .doOnSuccess { playerInstance -> playerInstance.action() }
            .subscribe()
            .also(internalDisposables::add)
    }

    private fun changeSongFavStatus(song: Song) {
        changeSongFavStatusUseCase.changeSongFavStatus(song)
            .subscribe()
            .let(internalDisposables::add)
    }

    /********************************
     ********* NOTIFICATION *********
     *******************************/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPlaybackNotificationChannel() {
        val safeManager = service.getSystemService(Context.NOTIFICATION_SERVICE)
            as? NotificationManager
            ?: return

        Logger.d(TAG, "Creating notification channel for playback")
        val channelName = service.getString(R.string.playback_channel_name)
        val channelDesc = service.getString(R.string.playback_channel_desc)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            description = channelDesc
        }
        safeManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendPrimaryNotification() {
        try {
            val notification = buildPrimaryNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                service.startForeground(
                    NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                service.startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "Failed to start foreground service", e)
            // TODO: fix foreground service limits
            // https://console.firebase.google.com/u/0/project/frolomuse/crashlytics/app/android:com.frolo.musp/issues/e723687c458aff1faae16651e007fa70?hl=ru&time=last-seven-days&versions=7.0.3-R%20(137)&types=crash&sessionEventKey=62F54C0400600001311DF27CB34CFE51_1709134770282028870
            service.stopSelf()
        }
    }

    private fun cancelNotification() {
        isNotificationCancelled = true
        playerInstance?.pause()
        Logger.d(TAG, "Notification cancelled. Stopping foreground")
        service.stopForeground(true)
        if (isBound.not()) {
            Logger.w(TAG, "No bound clients. STOP IT!")
            // No clients bound to the service. It may live only 60 seconds if it's Android API v26+
            // It makes no sense to continue running at all: no bound clients and no notification.
            // Then STOP IT.
            service.stopSelf()
        }
    }

    private fun buildPrimaryNotification(): Notification {
        return obtainPlayerNotificationBuilder(PlayerNotificationParams.NONE).build()
    }

    private fun buildPlayerNotification(params: PlayerNotificationParams): Notification {
        val builder = obtainPlayerNotificationBuilder(params)
        // The public version is the same
        val publicNotification = builder.build()
        return builder
            .setPublicVersion(publicNotification)
            .build()
    }

    private fun getPendingIntentFlags(): Int {
        return PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    }

    private fun newServicePendingIntent(requestCode: Int, intent: Intent): PendingIntent? {
        val flags: Int = getPendingIntentFlags()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(service, requestCode, intent, flags)
        } else {
            PendingIntent.getService(service, requestCode, intent, flags)
        }
    }

    private fun obtainPlayerNotificationBuilder(params: PlayerNotificationParams): NotificationCompat.Builder {
        val context: Context = service
        val item = params.item
        val art = params.art
        val isPlaying = params.isPlaying
        val isFav = params.isFavourite

        // Cancel notification
        val cancelPendingIntent = newServicePendingIntent(RC_CANCEL_NOTIFICATION,
            newIntent(context, PlayerServiceCmd.CMD_CANCEL_NOTIFICATION))
        // Change fav status
        val changeFavPendingIntent = newIntent(context, PlayerServiceCmd.CMD_CHANGE_FAV_STATUS).let { intent ->
            intent.putExtra(EXTRA_SONG, item?.toSong())
            newServicePendingIntent(RC_CHANGE_FAV_STATUS, intent)
        }
        // Skip to previous
        val prevPendingIntent = newServicePendingIntent(RC_SKIP_TO_PREVIOUS,
            newIntent(context, PlayerServiceCmd.CMD_SKIP_TO_PREVIOUS))
        // Toggle
        val togglePendingIntent = newServicePendingIntent(RC_TOGGLE,
            newIntent(context, PlayerServiceCmd.CMD_TOGGLE))
        // Skip to next
        val nextPendingIntent = newServicePendingIntent(RC_SKIP_TO_NEXT,
            newIntent(context, PlayerServiceCmd.CMD_SKIP_TO_NEXT))
        // Open player
        val openPendingIntent = PendingIntent.getActivity(service, RC_OPEN_PLAYER,
            MainActivity.newIntent(context, openPlayer = true), getPendingIntentFlags())

        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_player_notification_small)
            setContentTitle(item?.title.orEmpty())
            setContentText(item?.artist.orEmpty())
            setContentIntent(openPendingIntent)
            setPriority(NotificationCompat.PRIORITY_LOW)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // https://stackoverflow.com/a/73909180/9437681
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE

            addAction(if (isFav) R.drawable.ntf_ic_liked else R.drawable.ntf_ic_not_liked,
                "Change_Fav", changeFavPendingIntent)
            addAction(R.drawable.ntf_ic_previous, "Previous", prevPendingIntent)
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

        return notificationBuilder
    }

    override fun sendPlayerNotification(params: PlayerNotificationParams, forced: Boolean) {
        if (isNotificationCancelled && !forced) {
            return
        }

        val notification = buildPlayerNotification(params)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            service.startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            service.startForeground(NOTIFICATION_ID, notification)
        }

        // We're about to post the notification. It's not cancelled from now
        isNotificationCancelled = false
    }

    private class PlayerBinderImpl(
        private val playerSubject: BehaviorSubject<Player>
    ): Binder(), PlayerHolder {
        override fun peekPlayer(): Player? {
            return playerSubject.value
        }

        override fun getPlayerAsync(): Observable<Player> {
            return playerSubject
        }
    }

    companion object {
        private const val TAG = "PlayerService"

        private const val RC_CHANGE_FAV_STATUS = 201
        private const val RC_SKIP_TO_PREVIOUS = 202
        private const val RC_TOGGLE = 203
        private const val RC_SKIP_TO_NEXT = 204
        private const val RC_CANCEL_NOTIFICATION = 205
        private const val RC_OPEN_PLAYER = 206

        private const val EXTRA_CMD = "com.frolo.muse.player.service.CMD"
        private const val EXTRA_SONG = "com.frolo.muse.player.service.SONG"
        private const val EXTRA_IS_WIDGET_CALL = "com.frolo.muse.player.service.IS_WIDGET_CALL"

        private const val NOTIFICATION_CHANNEL_ID = "playback"
        private const val NOTIFICATION_ID = 1337

        @JvmStatic
        fun newIntent(context: Context): Intent = Intent(context, PlayerService::class.java)

        @JvmStatic
        fun newIntent(context: Context, @PlayerServiceCmd cmd: Int): Intent {
            return Intent(context, PlayerService::class.java).putExtra(EXTRA_CMD, cmd)
        }

        @JvmStatic
        fun newIntentFromWidget(context: Context, @PlayerServiceCmd cmd: Int): Intent {
            return Intent(context, PlayerService::class.java)
                .putExtra(EXTRA_CMD, cmd)
                .putExtra(EXTRA_IS_WIDGET_CALL, true)
        }
    }
}