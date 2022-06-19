package com.frolo.performance.anr

import android.os.Looper


fun interface OnAnrDetectedListener {
    /**
     * Called when an ANR is detected.
     */
    fun onAnrDetected(looper: Looper, info: AnrInfo)
}