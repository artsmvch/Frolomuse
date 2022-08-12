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
import com.frolo.mediascan.MediaScanService
import java.lang.ref.WeakReference


// TODO: check if this actually works
class ScanStatusObserver private constructor(
    context: Context?,
    private val lifecycleOwner: LifecycleOwner,
    private val onScanStarted: (() -> Unit)? = null,
    private val onScanCompleted: (() -> Unit)? = null,
    private val onScanCancelled: (() -> Unit)? = null
): BroadcastReceiver(), LifecycleObserver {

    private val contextRef = WeakReference(context)

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == MediaScanService.ACTION_MEDIA_SCANNING_STATUS) {
            when {
                intent.getBooleanExtra(MediaScanService.EXTRA_MEDIA_SCANNING_STARTED, false) ->
                    onScanStarted?.invoke()

                intent.getBooleanExtra(MediaScanService.EXTRA_MEDIA_SCANNING_COMPLETED, false) ->
                    onScanCompleted?.invoke()

                intent.getBooleanExtra(MediaScanService.EXTRA_MEDIA_SCANNING_CANCELLED, false) ->
                    onScanCancelled?.invoke()
            }
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

        private val intentFilter = IntentFilter(MediaScanService.ACTION_MEDIA_SCANNING_STATUS)

        fun observe(
            context: Context?,
            lifecycleOwner: LifecycleOwner,
            onScanStarted: (() -> Unit)? = null,
            onScanCompleted: (() -> Unit)? = null,
            onScanCancelled: (() -> Unit)? = null
        ) {
            ScanStatusObserver(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onScanStarted = onScanStarted,
                onScanCompleted = onScanCompleted,
                onScanCancelled = onScanCancelled
            ).startObserving()
        }

    }

}