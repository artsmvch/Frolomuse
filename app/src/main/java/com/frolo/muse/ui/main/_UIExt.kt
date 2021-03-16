package com.frolo.muse.ui.main

import android.content.Context
import android.media.AudioManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.dp2px
import com.frolo.muse.views.MarginItemDecoration


fun RecyclerView.decorateAsLinear(
        margin: Int = 2f.dp2px(context).toInt()) {
    addItemDecoration(MarginItemDecoration.createLinear(2 * margin, margin))
}

fun RecyclerView.decorateAsGrid(
        margin: Int = 2f.dp2px(context).toInt()) {
    addItemDecoration(MarginItemDecoration.createGrid(margin, margin))
}

fun Context.showVolumeControl() {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI)
}