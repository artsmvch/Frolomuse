package com.frolo.mediascan

import android.content.Context


object Scanners {
    @Deprecated(
        message = "Slow",
        replaceWith = ReplaceWith("createParallelScanner")
    )
    @JvmStatic
    fun createGradualScanner(
        context: Context,
        files: List<String>,
        timeoutMillis: Long,
        callback: Scanner.Callback
    ): Scanner {
        return GradualScannerImpl(context, files, timeoutMillis, callback)
    }

    @JvmStatic
    fun createParallelScanner(
        context: Context,
        files: List<String>,
        timeoutMillis: Long,
        callback: Scanner.Callback
    ): Scanner {
        return ParallelScannerImpl(context, files, timeoutMillis, callback)
    }
}