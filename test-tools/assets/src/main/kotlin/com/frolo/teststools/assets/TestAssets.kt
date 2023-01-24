package com.frolo.teststools.assets

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.test.InstrumentationRegistry
import com.frolo.mediascan.Scanner
import com.frolo.mediascan.Scanners
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal object TestAssets {
    private const val DIR_TEST_ASSETS = "frolomuse_test_assets"

    private fun getContext(): Context {
        return InstrumentationRegistry.getContext()
    }

    fun copyToExternalStorage() {
        val context = getContext()
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // throw IllegalStateException("write-external-storage permission not granted")
        }
        val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(externalDir, DIR_TEST_ASSETS)
        if (targetDir.exists() && !targetDir.isDirectory) {
            targetDir.delete()
        }
        if (!targetDir.exists() && !targetDir.mkdir()) {
            throw IllegalStateException("Failed to create assets dir")
        }

        val newFilePaths = ArrayList<String>()
        val assetsManager = context.assets
        listOf(
            "audio/mp3",
            "audio/mp4"
        ).forEach { relativeDirPath ->
            val dir = File(targetDir, relativeDirPath).apply { mkdirs() }
            assetsManager.list(relativeDirPath).orEmpty().toList().forEach { filename ->
                val targetFile = File(dir, filename).apply { createNewFile() }
                newFilePaths.add(targetFile.absolutePath)
                assetsManager.open("$relativeDirPath/$filename").use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
        blockingScanFiles(context, newFilePaths)
    }

    fun removeFromExternalStorage() {
        val targetDir = File(Environment.getExternalStorageDirectory(), DIR_TEST_ASSETS)
        targetDir.delete()
        blockingScanFiles(getContext(), listOf(targetDir.absolutePath))
    }

    private fun blockingScanFiles(context: Context, filePaths: List<String>) {
        val scanCountDownLatch = CountDownLatch(1)
        val scanCallback = object : Scanner.Callback {
            override fun onScanStarted() = Unit
            override fun onScanProgressChanged(total: Int, progress: Int) = Unit
            override fun onScanCompleted() {
                scanCountDownLatch.countDown()
            }
            override fun onScanCancelled() {
                scanCountDownLatch.countDown()
            }
        }
        Scanners.createParallelScanner(context, filePaths, scanCallback).start()
        scanCountDownLatch.await(10, TimeUnit.SECONDS)
    }
}