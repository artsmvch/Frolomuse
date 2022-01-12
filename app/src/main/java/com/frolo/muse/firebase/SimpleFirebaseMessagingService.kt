package com.frolo.muse.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import com.frolo.muse.Logger
import com.frolo.muse.R
import com.frolo.muse.android.ViewAppInStoreIntent
import com.frolo.muse.android.canStartActivity
import com.frolo.muse.android.notificationManager
import com.frolo.muse.di.appComponent
import com.frolo.muse.repository.FirebasePreferences
import com.frolo.muse.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import javax.inject.Inject


class SimpleFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firebasePreferences: FirebasePreferences

    private lateinit var mainHandler: Handler

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
        mainHandler = Handler(mainLooper)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        Logger.d(LOG_TAG, "Service created")
    }

    @WorkerThread
    override fun onNewToken(token: String) {
        Logger.d(LOG_TAG, "New token: $token")
        kotlin.runCatching {
            firebasePreferences.setMessagingToken(token).blockingAwait()
        }.onFailure { error ->
            Logger.e(error)
        }
    }

    @WorkerThread
    override fun onMessageReceived(message: RemoteMessage) {
        Logger.d(LOG_TAG, "Message received: $message")
        mainHandler.post {
            showRemoteMessage(message)
        }
    }

    @MainThread
    private fun showRemoteMessage(message: RemoteMessage) {
        val notificationPayload = extractNotificationPayload(message)
        if (notificationPayload != null) {
            val notification = buildNotification(notificationPayload) ?: return
            val notificationId = if (notificationPayload.useTimeAsId) {
                // will work til Tue Jan 19 2038 03:14:07 GMT+0000
                System.currentTimeMillis().toInt()
            } else {
                NOTIFICATION_ID
            }
            notificationManager?.notify(notificationId, notification)
        }
    }

    private fun extractNotificationPayload(message: RemoteMessage): NotificationPayload? {
        return try {
            val json = message.data["notification"]?.let(::JSONObject) ?: return null
            NotificationPayload(
                title = json.optString("title"),
                text = json.optString("text"),
                priority = json.optString("priority"),
                useTimeAsId = json.optBoolean("use_time_as_id", false),
                color = json.optString("color"),
                enableSound = json.optBoolean("enable_sound", false),
                enableVibration = json.optBoolean("enable_vibration", false),
                action = json.optString("action")
            )
        } catch (error: Throwable) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @MainThread
    private fun createNotificationChannel() {
        val manager = notificationManager ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.push_notifications_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            enableLights(false)
            description = getString(R.string.push_notifications_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    @MainThread
    private fun buildNotification(payload: NotificationPayload): Notification? {

        if (payload.title.isBlank() || payload.text.isBlank()) {
            // Nothing to show
            return null
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(payload.title)
            .setContentText(payload.text)
            .setSmallIcon(R.drawable.ic_player_notification_small)
            .setAutoCancel(true)
            .setOngoing(false)

        builder.priority = when (payload.priority) {
            "max" -> Notification.PRIORITY_MAX
            "high" -> Notification.PRIORITY_HIGH
            "default" -> Notification.PRIORITY_DEFAULT
            "low" -> Notification.PRIORITY_LOW
            "min" -> Notification.PRIORITY_MIN
            else -> Notification.PRIORITY_DEFAULT
        }

        if (payload.color.isNotBlank()) {
            kotlin.runCatching {
                Color.parseColor(payload.color)
            }.onSuccess { color ->
                builder.color = color
                builder.setColorized(true)
            }
        }

        if (payload.enableSound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        if (payload.enableVibration) {
            // ignored for now
        }

        when (payload.action) {
            ACTION_OPEN_APP -> {
                val intent = MainActivity.newIntent(this, true)
                val pendingIntent = PendingIntent.getActivity(
                        this, RC_HANDLE_NOTIFICATION_ACTION, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                builder.setContentIntent(pendingIntent)
            }

            ACTION_VIEW_APP_IN_STORE -> {
                val intent = ViewAppInStoreIntent(this)
                if (this.canStartActivity(intent)) {
                    val pendingIntent = PendingIntent.getActivity(
                            this, RC_HANDLE_NOTIFICATION_ACTION, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    builder.setContentIntent(pendingIntent)
                }
            }
        }

        return builder.build()
    }

    private data class NotificationPayload(
        val title: String,
        val text: String,
        val priority: String,
        val useTimeAsId: Boolean,
        val color: String,
        val enableSound: Boolean,
        val enableVibration: Boolean,
        val action: String
    )

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(LOG_TAG, "Service destroyed")
    }

    companion object {
        private const val LOG_TAG = "SimpleFirebaseMessaging"

        private const val CHANNEL_ID = "push_notifications"
        private const val NOTIFICATION_ID = 3715

        // Actions
        private const val ACTION_OPEN_APP = "open_app"
        private const val ACTION_VIEW_APP_IN_STORE = "view_app_in_store"

        // Request codes
        private const val RC_HANDLE_NOTIFICATION_ACTION = 5317
    }

}