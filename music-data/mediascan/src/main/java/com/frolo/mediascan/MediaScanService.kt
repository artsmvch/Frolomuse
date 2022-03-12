package com.frolo.mediascan

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.util.SparseArray
import android.widget.RemoteViews
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.frolo.debug.DebugUtils
import com.frolo.threads.ThreadStrictMode
import java.lang.Exception
import java.util.ArrayList


class MediaScanService : Service() {

    private var isAlive = false
    private var notificationManager: NotificationManager? = null
    private var engineThread: Thread? = null
    private var engineHandler: Handler? = null
    private var mainHandler: Handler? = null
    @GuardedBy("scanners")
    private val scanners = SparseArray<ScannerInfo>()

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        // Do not forget to start foreground!
        val notification = createPreparationNotification()
        startForeground(NOTIFICATION_ID_MEDIA_SCANNER, notification)

        val thread = HandlerThread("MediaScanner", Process.THREAD_PRIORITY_DEFAULT)
        thread.start()
        engineThread = thread
        engineHandler = Handler(thread.looper)
        mainHandler = Handler(Looper.getMainLooper())
        isAlive = true

        if (DEBUG) Log.d(LOG_TAG, "Services created")
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val manager = this.notificationManager ?: return
        val channel = NotificationChannel(
            CHANNEL_ID_MEDIA_SCANNER,
            getString(R.string.media_scanner_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = getString(R.string.media_scanner_channel_desc)
        channel.setShowBadge(false)
        channel.setSound(null, null)
        channel.enableVibration(false)
        channel.enableLights(false)
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (DEBUG) Log.d(LOG_TAG, "Handle intent: $action")
        if (ACTION_CANCEL_SCAN_MEDIA == action) {
            cancelAllScanners()
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        } else if (ACTION_SCAN_MEDIA == action) {
            cancelAllScanners()
            onPrepareScanner(startId)
            if (intent.hasExtra(EXTRA_FILES)) {
                val targetFiles: List<String>? = intent.getStringArrayListExtra(EXTRA_FILES)
                collectAndScanFiles(
                    startId = startId,
                    fullRescan = false,
                    targetFiles = targetFiles
                )
            } else {
                collectAndScanFiles(
                    startId = startId,
                    fullRescan = true,
                    targetFiles = null
                )
            }
            return START_REDELIVER_INTENT
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAllScanners()
        stopForeground(true)
        isAlive = false
        notificationManager = null
        engineHandler?.removeCallbacksAndMessages(null)
        engineHandler = null
        engineThread?.interrupt()
        engineThread = null
        mainHandler?.removeCallbacksAndMessages(null)
        mainHandler = null
        if (DEBUG) Log.d(LOG_TAG, "Service destroyed")
    }

    /**
     * Cancels all active scanners and removes them then.
     */
    private fun cancelAllScanners() {
        synchronized(scanners) {
            scanners.forEach { _, info -> info.scanner.cancel() }
            scanners.clear()
        }
    }

    /**
     * Collects and scans files on the device that need to be scanned.
     * If [fullRescan] is true then all files in the device will be collected.
     * Otherwise, only files from [targetFiles] (including their nested child files)
     * will be collected, if not null.
     *
     * Note that collection is performed on the engine thread,
     * and then the async scanning is started on the main thread.
     * @param startId id of the command
     * @param fullRescan true if this should collect all the files on the device
     * @param targetFiles from which to collect files for scanning, ignored if [fullRescan] is true
     */
    private fun collectAndScanFiles(
        startId: Int,
        fullRescan: Boolean,
        targetFiles: List<String>?
    ) {
        val task = Runnable {
            ThreadStrictMode.assertBackground()
            if (DEBUG) Log.w(LOG_TAG, "Collect files to scan...")
            val files: List<String> = try {
                val collector = AudioFileCollector.get(this@MediaScanService)
                when {
                    fullRescan -> collector.collectAll()
                    targetFiles != null -> collector.collectFrom(targetFiles)
                    else -> emptyList()
                }
            } catch (e: Exception) {
                if (DEBUG) Log.e(LOG_TAG, "Failed to collect files", e)
                emptyList<String>()
            }
            if (Thread.interrupted()) {
                if (DEBUG) Log.w(LOG_TAG, "Engine thread is interrupted")
                // Thread interrupted => cancel scanning
                return@Runnable
            }
            mainHandler?.post { scanAsync(startId, fullRescan, files) }
        }
        engineHandler?.post(task)
    }

    /**
     * Starts an asynchronous scan of the given [files].
     * This creates and saves a [ScannerInfo] that is associated with [startId].
     * @param startId id of the command
     * @param fullRescan full rescan?
     * @param files to scan
     */
    @MainThread
    private fun scanAsync(startId: Int, fullRescan: Boolean, files: List<String>) {
        ThreadStrictMode.assertMain()
        if (!isAlive) {
            // Service is not created yet or destroyed already
            return
        }

        // We need to pass the application context to avoid memory leak issues.
        // See https://stackoverflow.com/questions/5739140/mediascannerconnection-produces-android-app-serviceconnectionleaked
        val appContext = this.applicationContext
        val callback: Scanner.Callback = object : Scanner.Callback {
            override fun onScanStarted() = onScanStarted(startId, files.size)

            override fun onScanProgressChanged(total: Int, progress: Int) =
                onScanProgressChanged(startId, total, progress)

            override fun onScanCompleted() = onScanCompleted(startId)

            override fun onScanCancelled() = onScanCancelled(startId)
        }
        val newScanner = Scanners.createTimedScanner(appContext, files, SCAN_TIMEOUT_MILLIS, callback)
        val newScannerInfo = ScannerInfo(startId, fullRescan, newScanner)
        synchronized(scanners) {
            scanners[startId]?.also { oldScannerInfo ->
                oldScannerInfo.scanner.cancel()
            }
            newScannerInfo.scanner.start()
            scanners.put(startId, newScannerInfo)
        }
        if (DEBUG) Log.d(LOG_TAG, "Async scan started: startId=$startId")
    }

    @MainThread
    private fun onPrepareScanner(startId: Int) {
        ThreadStrictMode.assertMain()
        if (DEBUG) Log.d(LOG_TAG, "Prepare: startId=$startId")
        val notification = createPreparationNotification()
        notificationManager?.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification)
    }

    @MainThread
    private fun onScanStarted(startId: Int, totalCount: Int) {
        ThreadStrictMode.assertMain()
        if (DEBUG) Log.d(LOG_TAG, "Scan started: startId=$startId, totalCount=$totalCount")
        if (isAlive) {
            val statusIntent = Intent(ACTION_MEDIA_SCANNING_STATUS)
                .putExtra(EXTRA_MEDIA_SCANNING_STARTED, true)
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent)
        }
        val notification = createProgressNotification(totalCount, 0)
        notificationManager?.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification)
    }

    @MainThread
    private fun onScanProgressChanged(startId: Int, totalCount: Int, progress: Int) {
        ThreadStrictMode.assertMain()
        if (DEBUG) Log.d(LOG_TAG, "Scan progress changed: " +
                "startId=$startId, totalCount=$totalCount, progress=$progress")
        val notification = createProgressNotification(totalCount, progress)
        notificationManager?.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification)
    }

    @MainThread
    private fun onScanCompleted(startId: Int) {
        ThreadStrictMode.assertMain()
        if (DEBUG) Log.d(LOG_TAG, "Scan completed: startId=$startId")
        if (isAlive) {
            val statusIntent = Intent(ACTION_MEDIA_SCANNING_STATUS)
                .putExtra(EXTRA_MEDIA_SCANNING_COMPLETED, true)
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent)
        }
        synchronized(scanners) { scanners.remove(startId) }
        stopSelf(startId)
    }

    @MainThread
    private fun onScanCancelled(startId: Int) {
        ThreadStrictMode.assertMain()
        if (DEBUG) Log.d(LOG_TAG, "Scan cancelled: startId=$startId")
        if (isAlive) {
            val statusIntent = Intent(ACTION_MEDIA_SCANNING_STATUS)
                .putExtra(EXTRA_MEDIA_SCANNING_CANCELLED, true)
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent)
        }
        synchronized(scanners) { scanners.remove(startId) }
        stopSelf(startId)
    }

    private fun createPreparationNotification(): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_media_scan_preparing)
        val cancelPendingIntent = PendingIntent.getService(this, RC_CANCEL,
            newCancelIntent(this), PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_cancel, cancelPendingIntent)
        remoteViews.setProgressBar(R.id.pb_progress, 0, 0, true)
        return NotificationCompat.Builder(this, CHANNEL_ID_MEDIA_SCANNER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(getString(R.string.preparing_files_to_scan))
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSmallIcon(R.drawable.ic_scan_file)
            .build()
    }

    private fun createProgressNotification(total: Int, progress: Int): Notification {
        val remoteViews = RemoteViews(packageName, R.layout.notification_media_scan_scanning)
        remoteViews.setTextViewText(R.id.tv_message, getString(R.string.scanning_media_storage))
        remoteViews.setTextViewText(R.id.tv_progress, "$progress/$total")
        val cancelPendingIntent = PendingIntent.getService(this, RC_CANCEL,
            newCancelIntent(this), PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_cancel, cancelPendingIntent)
        remoteViews.setProgressBar(R.id.pb_progress, total, progress, false)
        return NotificationCompat.Builder(this, CHANNEL_ID_MEDIA_SCANNER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(getString(R.string.scanning_media_storage))
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSmallIcon(R.drawable.ic_scan_file)
            .build()
    }

    private class ScannerInfo(
        val startId: Int,
        val fullRescan: Boolean,
        val scanner: Scanner
    )

    companion object {
        private val DEBUG = DebugUtils.isDebug()
        private val LOG_TAG = MediaScanService::class.java.simpleName

        // Timeout for one-file-scan
        private const val SCAN_TIMEOUT_MILLIS = 5000L
        private const val RC_CANCEL = 3731

        // Used for broadcasting
        const val ACTION_MEDIA_SCANNING_STATUS = "com.frolo.mediascan.ACTION_MEDIA_SCANNING_STATUS"
        private const val ACTION_SCAN_MEDIA = "com.frolo.mediascan.ACTION_SCAN_MEDIA"
        private const val ACTION_CANCEL_SCAN_MEDIA = "com.frolo.mediascan.ACTION_CANCEL_SCAN_MEDIA"

        const val EXTRA_MEDIA_SCANNING_STARTED = "media_scanning_started"
        const val EXTRA_MEDIA_SCANNING_COMPLETED = "media_scanning_completed"
        const val EXTRA_MEDIA_SCANNING_CANCELLED = "media_scanning_cancelled"

        private const val EXTRA_FILES = "files"

        private const val CHANNEL_ID_MEDIA_SCANNER = "media_scanner"
        private const val NOTIFICATION_ID_MEDIA_SCANNER = 1735

        fun start(context: Context) {
            val intent = Intent(context, MediaScanService::class.java)
                .setAction(ACTION_SCAN_MEDIA)
            ContextCompat.startForegroundService(context, intent)
        }

        fun start(context: Context, targetFiles: ArrayList<String>) {
            val intent = Intent(context, MediaScanService::class.java)
                .setAction(ACTION_SCAN_MEDIA)
                .putExtra(EXTRA_FILES, targetFiles)
            ContextCompat.startForegroundService(context, intent)
        }

        fun start(context: Context, targetFile: String) {
            val targetFiles = ArrayList<String>(1)
            targetFiles.add(targetFile)
            start(context, targetFiles)
        }

        private fun newCancelIntent(context: Context): Intent {
            return Intent(context, MediaScanService::class.java)
                .setAction(ACTION_CANCEL_SCAN_MEDIA)
        }
    }
}