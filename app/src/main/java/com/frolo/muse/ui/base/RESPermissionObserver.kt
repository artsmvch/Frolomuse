package com.frolo.muse.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.lang.ref.WeakReference


/**
 * Observes ReadExternalStorage permission status.
 */
@Deprecated("")
class RESPermissionObserver  private constructor(
    context: Context?,
    private val lifecycleOwner: LifecycleOwner,
    private val onPermissionGranted: () -> Unit
): BroadcastReceiver(), LifecycleObserver {

    private val contextRef = WeakReference(context)

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == ACTION_RES_PERMISSION_GRANTED) {
            onPermissionGranted.invoke()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStarted() {
        contextRef.get()?.also { safeContext ->
            LocalBroadcastManager.getInstance(safeContext)
                    .registerReceiver(this, intentFilter)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopped() {
        contextRef.get()?.also { safeContext ->
            LocalBroadcastManager.getInstance(safeContext)
                    .unregisterReceiver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroyed() {
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    fun startObserving() {
        val lifecycle = lifecycleOwner.lifecycle
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            lifecycle.addObserver(this)
        }
    }

    companion object {

        // RES stands for Read External Storage
        private const val ACTION_RES_PERMISSION_GRANTED = "com.frolo.muse.ui.base.RES_PERMISSION_GRANTED"

        private val intentFilter = IntentFilter(ACTION_RES_PERMISSION_GRANTED)

        fun observe(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            onPermissionGranted: () -> Unit
        ) {
            RESPermissionObserver(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onPermissionGranted = onPermissionGranted
            ).startObserving()
        }

        fun dispatchGranted(context: Context) {
            val intent = Intent(ACTION_RES_PERMISSION_GRANTED)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

    }

}