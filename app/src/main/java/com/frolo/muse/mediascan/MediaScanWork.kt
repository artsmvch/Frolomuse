package com.frolo.muse.mediascan

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.work.*
import com.frolo.muse.Logger
import com.frolo.threads.ThreadStrictMode
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val LOG_TAG = "MediaScanWork"

private const val WORK_NAME_SCAN_ALL  = "com.frolo.muse.mediascan.WORK_NAME_SCAN_ALL"

private const val ARG_TARGET_PATHS = "com.frolo.muse.mediascan.target_paths"
private const val ARG_SCAN_ALL_PATHS = "com.frolo.muse.mediascan.scan_all_paths"

private const val FILE_SCAN_TIMEOUT_MS = 10_000L

fun scheduleMediaScanWork(context: Context) {
    val inputData = Data.Builder()
        .putBoolean(ARG_SCAN_ALL_PATHS, true)
        .build()
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()
    val mediaScanWorkRequest: PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<MediaScanWorker>(1, TimeUnit.DAYS)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()
    WorkManager
        .getInstance(context)
        .enqueueUniquePeriodicWork(WORK_NAME_SCAN_ALL,
            ExistingPeriodicWorkPolicy.KEEP, mediaScanWorkRequest)
}

internal class MediaScanWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val data = this.inputData
        val fileCollector = AudioFileCollector.get(applicationContext)
        return if (data.getBoolean(ARG_SCAN_ALL_PATHS, false)) {
            doScan(fileCollector.collectAll())
        } else {
            val targetFiles = data.getStringArray(ARG_TARGET_PATHS).orEmpty().toList()
            doScan(fileCollector.collectFrom(targetFiles))
        }
    }

    @WorkerThread
    private fun doScan(files: List<String>): Result {
        ThreadStrictMode.assertBackground()
        val fileTimeoutMillis = FILE_SCAN_TIMEOUT_MS
        val waiter = CountDownLatch(1)
        val callback = object : Scanner.Callback {
            override fun onScanStarted() {
                Logger.d(LOG_TAG, "Scan started")
            }

            override fun onScanProgressChanged(total: Int, progress: Int) {
            }

            override fun onScanCompleted() {
                Logger.d(LOG_TAG, "Scan completed")
                waiter.countDown()
            }

            override fun onScanCancelled() {
                Logger.d(LOG_TAG, "Scan cancelled")
                waiter.countDown()
            }
        }
        val timedScanner = Scanners.createTimedScanner(applicationContext, files, fileTimeoutMillis, callback)
        timedScanner.start()
        waiter.await(files.size * fileTimeoutMillis, TimeUnit.MILLISECONDS)
        return Result.success()
    }
}