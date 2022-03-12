package com.frolo.mediascan

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.annotation.AnyThread
import com.frolo.debug.DebugUtils
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


@Deprecated(
    message = "Slow",
    replaceWith = ReplaceWith("ParallelScannerImpl")
)
internal class GradualScannerImpl constructor(
    context: Context,
    files: List<String>,
    private val timeoutMillis: Long,
    private val callback: Scanner.Callback
): Scanner {

    private val isStarted = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)

    private val pathCount: Int = files.count()
    private val pendingPaths: BlockingQueue<String> = LinkedBlockingQueue(files)

    // The path that is currently being scanned
    private val currentPath = AtomicReference<String?>()
    private val clientProxy: MediaScannerConnection.MediaScannerConnectionClient =
        object : MediaScannerConnection.MediaScannerConnectionClient {
            override fun onMediaScannerConnected() {
                if (!isCancelled.get()) {
                    if (DEBUG) Log.d(LOG_TAG, "Connected to MediaScanner")
                    postTask(dispatchScanStartedTask, dispatchScanStartedToken)
                    scanNextPath()
                }
            }

            override fun onScanCompleted(path: String?, uri: Uri?) {
                if (path != currentPath.get()) {
                    // That's not the path we're expecting
                    return
                }
                if (!isCancelled.get()) {
                    postTask(dispatchProgressChangedTask, dispatchProgressChangedToken)
                    scanNextPath()
                }
            }
        }
    private val connection: MediaScannerConnection = MediaScannerConnection(context, clientProxy)

    private val handler: Handler = Handler(context.mainLooper)
    // Check timeout msg
    private val checkTimeoutToken = Any()
    private val checkTimeoutTask = Runnable {
        val problematicPath = currentPath.getAndSet(null)
        if (DEBUG) Log.w(LOG_TAG, "Timeout for $problematicPath. Keep on scanning")
        scanNextPath()
    }
    // Scan started msg
    private val dispatchScanStartedToken = Any()
    private val dispatchScanStartedTask = Runnable { callback.onScanStarted() }
    // Progress changed msg
    private val dispatchProgressChangedToken = Any()
    private val dispatchProgressChangedTask = Runnable {
        val progress = pathCount - pendingPaths.size
        callback.onScanProgressChanged(pathCount, progress)
    }
    // Scan completed msg
    private val dispatchScanCompletedToken = Any()
    private val dispatchScanCompleted = Runnable { callback.onScanCompleted() }
    // Scan cancelled msg
    private val dispatchScanCancelledToken = Any()
    private val dispatchScanCancelled = Runnable { callback.onScanCancelled() }

    override fun start() {
        if (!isStarted.getAndSet(true)) {
            connection.connect()
            if (DEBUG) Log.d(LOG_TAG, "Connecting to MediaScanner. Path count=" + pendingPaths.size)
        } else {
            if (DEBUG) Log.w(LOG_TAG, "Cannot start scanning, because it has been started already")
        }
    }

    override fun cancel() {
        completeInternal(true)
    }

    @AnyThread
    private fun scanNextPath() {
        handler.removeCallbacksAndMessages(checkTimeoutToken)
        if (isCancelled.get() || !connection.isConnected) {
            // It's over
            return
        }
        val path = pendingPaths.poll()
        if (path == null) {
            // The queue is empty
            completeInternal(false)
        } else {
            if (DEBUG) Log.d(LOG_TAG, "Scanning " + path + ". " + pendingPaths.size + " paths left")
            currentPath.set(path)
            connection.scanFile(path, null)
            postTask(handler, checkTimeoutTask, checkTimeoutToken, timeoutMillis)
        }
    }

    @AnyThread
    private fun completeInternal(cancelled: Boolean) {
        if (!isCancelled.getAndSet(true)) {
            connection.disconnect()
        }
        if (DEBUG) Log.d(LOG_TAG, "Complete: cancelled=$cancelled")
        if (cancelled) {
            postTask(dispatchScanCancelled, dispatchScanCancelledToken)
        } else {
            postTask(dispatchScanCompleted, dispatchScanCompletedToken)
        }
    }

    private fun postTask(task: Runnable, token: Any) {
        handler.removeCallbacksAndMessages(token)
        if (handler.looper.thread === Thread.currentThread()) {
            task.run()
        } else {
            handler.postAtTime(task, token, SystemClock.uptimeMillis())
        }
    }

    private fun postTask(handler: Handler, task: Runnable, token: Any, delay: Long) {
        handler.removeCallbacksAndMessages(token)
        handler.postAtTime(task, token, SystemClock.uptimeMillis() + delay)
    }

    companion object {
        private val DEBUG = DebugUtils.isDebug()
        private val LOG_TAG = GradualScannerImpl::class.java.simpleName
    }
}