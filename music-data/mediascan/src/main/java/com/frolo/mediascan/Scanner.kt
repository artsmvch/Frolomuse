package com.frolo.mediascan

interface Scanner {
    fun start()
    fun cancel()

    interface Callback {
        fun onScanStarted()
        fun onScanProgressChanged(total: Int, progress: Int)
        fun onScanCompleted()
        fun onScanCancelled()
    }
}