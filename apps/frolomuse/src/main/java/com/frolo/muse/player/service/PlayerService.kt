package com.frolo.muse.player.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.frolo.muse.player.service.PlayerService.Companion.newIntent


/**
 * The heart of the app. It must be running as long as the app is alive.
 *
 * Here is the whole logic of playing, navigating and dispatching events.
 *
 * Communication with the service can be through binding to a special binder
 * returned by the [PlayerServiceDelegate.onBind] method or intents (see [newIntent]]).
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

    private val delegate: PlayerServiceDelegate by lazy { PlayerServiceDelegate(this) }

    override fun onBind(intent: Intent?): IBinder {
        return delegate.onBind(intent)
    }

    override fun onRebind(intent: Intent?) {
        delegate.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return delegate.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        delegate.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return delegate.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        delegate.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        delegate.onDestroy()
    }

    companion object {

        /**
         * Starts the PlayerService in foreground.
         */
        @JvmStatic
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, newIntent(context))
        }

        @JvmStatic
        fun newIntent(context: Context): Intent =
            PlayerServiceDelegate.newIntent(context)

        @JvmStatic
        fun newIntent(context: Context, @PlayerServiceCmd cmd: Int): Intent =
            PlayerServiceDelegate.newIntent(context, cmd)

        /**
         * A variant for [newIntent], which specifies that the intent will be called from the app widget.
         */
        @JvmStatic
        fun newIntentFromWidget(context: Context, @PlayerServiceCmd cmd: Int): Intent =
            PlayerServiceDelegate.newIntentFromWidget(context, cmd)

    }

}