package com.frolo.muse

import android.os.Handler


/**
 * Delegates the call to [Handler.removeCallbacks] only if [r] is not null.
 */
fun Handler.removeCallbacksSafely(r: Runnable?) {
    r?.also { removeCallbacks(r) }
}