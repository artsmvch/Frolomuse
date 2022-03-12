package com.frolo.mediascan

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.frolo.debug.DebugUtils
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


internal class ParallelScannerImpl(
    private val context: Context,
    private val files: List<String>,
    private val timeoutMillis: Long,
    private val callback: Scanner.Callback
): Scanner {
    
    private val isStarted = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)

    private val totalCount = files.count()
    private val remainingFileCounter = AtomicInteger(totalCount)

    private val handler = Handler(context.mainLooper)

    private val clientProxy: MediaScannerConnection.MediaScannerConnectionClient =
        object : MediaScannerConnection.MediaScannerConnectionClient {
            override fun onMediaScannerConnected() {
                if (isCancelled.get()) {
                    return
                }
                if (DEBUG) Log.d(LOG_TAG, "Connected to MediaScanner")
                postTask { callback.onScanStarted() }
                files.forEach { file ->
                    connection.scanFile(file, null)
                }
            }

            override fun onScanCompleted(path: String, uri: Uri) {
                if (isCancelled.get()) {
                    return
                }
                val remaining = remainingFileCounter.decrementAndGet()
                if (DEBUG) Log.d(LOG_TAG, "File scan completed: remaining=$remaining")
                if (remaining > 0) {
                    postTask {
                        callback.onScanProgressChanged(
                            total = totalCount,
                            progress = totalCount - remaining
                        )
                    }
                } else if (remaining == 0) {
                    if (DEBUG) Log.d(LOG_TAG, "Completed scanning")
                    postTask { callback.onScanCompleted() }
                }
            }
        }
    private val connection: MediaScannerConnection = MediaScannerConnection(context, clientProxy)
    
    override fun start() {
        if (isCancelled.get()) {
            return
        }
        if (isStarted.getAndSet(true)) {
            return
        }
        startActual()
    }

    override fun cancel() {
        if (isCancelled.getAndSet(true)) {
            return
        }
        cancelActual()
    }

    private fun startActual() {
        connection.connect()
        if (DEBUG) Log.d(LOG_TAG, "Started scanning")
    }
    
    private fun cancelActual() {
        connection.disconnect()
        if (DEBUG) Log.d(LOG_TAG, "Canceled scanning")
    }

    private fun postTask(task: Runnable) {
        handler.post(task)
    }
    
    companion object {
        private val DEBUG = DebugUtils.isDebug()
        private val LOG_TAG = ParallelScannerImpl::class.java.simpleName
    }
}