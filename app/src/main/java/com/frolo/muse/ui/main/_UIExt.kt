package com.frolo.muse.ui.main

import android.content.Context
import android.media.AudioManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.dp2px
import com.frolo.muse.views.MarginItemDecoration


fun RecyclerView.addLinearItemMargins(
    horizontalMargin: Int = 0,
    verticalMargin: Int = 2f.dp2px(context).toInt()
) {
    addItemDecoration(MarginItemDecoration.createLinear(horizontalMargin, verticalMargin))
}

fun RecyclerView.addGridItemMargins(
    horizontalMargin: Int = 2f.dp2px(context).toInt(),
    verticalMargin: Int = 2f.dp2px(context).toInt()
) {
    addItemDecoration(MarginItemDecoration.createGrid(horizontalMargin, verticalMargin))
}

fun Context.showVolumeControl() {
    val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI)
}