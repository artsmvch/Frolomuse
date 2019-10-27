package com.frolo.muse.ui.main

import android.content.Context
import android.media.AudioManager
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.ui.toPx
import com.frolo.muse.views.MarginItemDecoration


fun RecyclerView.decorateAsLinear(
        margin: Int = 2f.toPx(context).toInt()) {
    addItemDecoration(MarginItemDecoration.createLinear(2 * margin, margin))
}

fun RecyclerView.decorateAsGrid(
        margin: Int = 2f.toPx(context).toInt()) {
    addItemDecoration(MarginItemDecoration.createGrid(margin, margin))
}

fun RecyclerView.overrideAnimationDuration(
        addDuration: Long = 120L,
        removeDuration: Long = 120L,
        moveDuration: Long = 250L,
        changeDuration: Long = 250L) {
    itemAnimator?.also { safeAnimator ->
        safeAnimator.addDuration = addDuration
        safeAnimator.removeDuration = removeDuration
        safeAnimator.moveDuration = moveDuration
        safeAnimator.changeDuration = changeDuration
    }
}

fun Context.showVolumeControl() {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI)
}

fun FragmentManager.removeAllFragmentsNow() {
    val transaction = beginTransaction()

    fragments.forEach { fragment ->
        transaction.remove(fragment)
    }

    transaction.commitNow()
}