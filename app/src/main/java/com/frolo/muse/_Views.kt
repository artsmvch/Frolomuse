package com.frolo.muse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes


/**
 * Inflates a view for the given [layoutId] applying default layout params of [this] view group.
 * NOTE: this does not attach the inflated child to [this] view group.
 */
fun ViewGroup.inflateChild(@LayoutRes layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}

/**
 * Delegates the call to [View.removeCallbacks] only if [action] is not null.
 */
fun View.removeCallbacksSafely(action: Runnable?) {
    if (action != null) removeCallbacks(action)
}
