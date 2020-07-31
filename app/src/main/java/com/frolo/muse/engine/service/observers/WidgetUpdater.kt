package com.frolo.muse.engine.service.observers

import android.content.Context
import com.frolo.muse.ThreadStrictMode
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.frolo.muse.widget.PlayerWidget3Provider
import com.frolo.muse.widget.PlayerWidget4Provider
import java.lang.ref.WeakReference


/**
 * Observes the state of the player and updates the player widgets,
 * namely [PlayerWidget3Provider] and [PlayerWidget4Provider], as needed.
 */
class WidgetUpdater constructor(context: Context): SimplePlayerObserver() {

    private val contextRef = WeakReference(context)

    private fun updateWidgets(player: Player) {
        ThreadStrictMode.assertMain()
        contextRef.get()?.also { safeContext ->
            PlayerWidget3Provider.update(safeContext, player)
            PlayerWidget4Provider.update(safeContext, player)
        }
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