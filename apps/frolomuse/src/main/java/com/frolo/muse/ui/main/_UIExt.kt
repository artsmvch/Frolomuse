package com.frolo.muse.ui.main

import android.content.Context
import android.media.AudioManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.ui.Screen
import com.frolo.muse.views.MarginItemDecoration

private const val DEFAULT_ITEM_MARGIN_IN_DP = 2

fun RecyclerView.addLinearItemMargins(
    horizontalMargin: Int = 0,
    verticalMargin: Int = Screen.dp(context, DEFAULT_ITEM_MARGIN_IN_DP)
) {
    addItemDecoration(MarginItemDecoration.createLinear(horizontalMargin, verticalMargin))
}

fun RecyclerView.addGridItemMargins(
    horizontalMargin: Int = Screen.dp(context, DEFAULT_ITEM_MARGIN_IN_DP),
    verticalMargin: Int = Screen.dp(context, DEFAULT_ITEM_MARGIN_IN_DP)
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