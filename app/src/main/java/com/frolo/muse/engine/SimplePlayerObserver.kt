package com.frolo.muse.engine

import com.frolo.muse.model.media.Song


abstract class SimplePlayerObserver: PlayerObserver {
    override fun onPrepared(player: Player) = Unit

    override fun onPlaybackStarted(player: Player) = Unit

    override fun onPlaybackPaused(player: Player) = Unit

    override fun onSoughtTo(player: Player, position: Int) = Unit

    override fun onQueueChanged(player: Player, queue: SongQueue) = Unit

    override fun onSongChanged(player: Player, song: Song?, positionInQueue: Int) = Unit

    override fun onShuffleModeChanged(player: Player, mode: Int) = Unit

    override fun onRepeatModeChanged(player: Player, mode: Int) = Unit

    override fun onShutdown(player: Player) = Unit

    override fun onABChanged(player: Player, aPointed: Boolean, bPointed: Boolean) = Unit
}