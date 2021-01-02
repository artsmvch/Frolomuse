package com.frolo.muse.engine.service.observers

import android.content.Context
import com.frolo.muse.ThreadStrictMode
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.widget.PlayerWidgetProvider


/**
 * Observes the state of the player and updates the player widgets, namely [PlayerWidgetProvider], as needed.
 */
class WidgetUpdater constructor(
    private val context: Context
): SimplePlayerObserver() {

    private fun updateWidgets(player: Player) {
        ThreadStrictMode.assertMain()
        PlayerWidgetProvider.update(context, player)
    }

    override fun onPlaybackStarted(player: Player) {
        updateWidgets(player)
    }

    override fun onPlaybackPaused(player: Player) {
        updateWidgets(player)
    }

    override fun onAudioSourceChanged(player: Player, item: AudioSource?, positionInQueue: Int) {
        updateWidgets(player)
    }

    override fun onShuffleModeChanged(player: Player, mode: Int) {
        updateWidgets(player)
    }

    override fun onRepeatModeChanged(player: Player, mode: Int) {
        updateWidgets(player)
    }

    override fun onShutdown(player: Player) {
        updateWidgets(player)
    }

}