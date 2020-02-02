package com.frolo.muse

import android.os.Handler


fun Handler.safelyRemoveCallbacks(r: Runnable?) {
    r?.also { removeCallbacks(r) }
}