package com.frolo.core.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.forEach


/**
 * Inflates a view for the given [layoutId] applying default layout params of [this] view group.
 * NOTE: this does not attach the inflated child to [this] view group.
 */
fun ViewGroup.inflateChild(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

/**
 * Delegates the call to [View.removeCallbacks] only if [action] is not null.
 */
fun View.removeCallbacksSafely(action: Runnable?) {
    if (action != null) removeCallbacks(action)
}

fun View.doTraversal(action: (View) -> Unit) {
    action.invoke(this)
    if (this is ViewGroup) {
        this.forEach { child ->
            child.doTraversal(action)
        }
    }
}
