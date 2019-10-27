package com.frolo.muse.engine

import com.frolo.muse.model.media.Song


interface PlayerObserver {
    fun onPrepared(player: Player)
    fun onPlaybackStarted(player: Player)
    fun onPlaybackPaused(player: Player)
    fun onSoughtTo(player: Player, position: Int) // Called only when sought by user
    fun onQueueChanged(player: Player, queue: SongQueue)
    fun onSongChanged(player: Player, song: Song?, positionInQueue: Int)
    fun onShuffleModeChanged(player: Player, @Player.ShuffleMode mode: Int)
    fun onRepeatModeChanged(player: Player, @Player.RepeatMode mode: Int)
    fun onShutdown(player: Player)
    fun onABChanged(player: Player, aPointed: Boolean, bPointed: Boolean)
}