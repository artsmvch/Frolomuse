package com.frolo.muse.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface PlayerObserver {

    /**
     * Called when <code>player</code> has been prepared.
     * From this point until {@link PlayerObserver#onAudioSourceChanged(Player, AudioSource, int)} method is called,
     * calling the {@link Player#getProgress()}, {@link Player#getDuration()}, {@link Player#isPlaying()} methods really makes sense.
     * @param player player that is prepared
     */
    void onPrepared(@NotNull Player player);

    /**
     * Called when <code>player</code> starts playing.
     * @param player that starts playing
     */
    void onPlaybackStarted(@NotNull Player player);

    /**
     * Called when <code>player</code> pauses playback.
     * @param player that pauses playback
     */
    void onPlaybackPaused(@NotNull Player player);

    /**
     * Called when the user has positioned the playback at the given <code>position</code>.
     * NOTE: called only when the user changes the position himself.
     * @param player on which the user has positioned the playback at the position
     */
    void onSoughtTo(@NotNull Player player, int position);

    /**
     * Called when the current audio source queue gets changed for the given <code>player</code>.
     * @param player for which the current queue is changed
     * @param queue new audio source queue
     */
    void onQueueChanged(@NotNull Player player, @NotNull AudioSourceQueue queue);

    /**
     * Called when the current audio source gets changed for the given <code>player</code>.
     * @param player for which the current audio source is changed
     * @param item new audio source
     * @param positionInQueue position of the new audio source in the current queue
     */
    void onAudioSourceChanged(@NotNull Player player, @Nullable AudioSource item, int positionInQueue);

    /**
     * Called when the current shuffle mode gets changed for the given <code>player</code>.
     * @param player for which the current shuffle mode is changed
     * @param mode new shuffle mode
     */
    void onShuffleModeChanged(@NotNull Player player, @Player.ShuffleMode int mode);

    /**
     * Called when the current repeat mode gets changed for the given <code>player</code>.
     * @param player for which the current repeat mode is changed
     * @param mode new repeat mode
     */
    void onRepeatModeChanged(@NotNull Player player, @Player.RepeatMode int mode);

    /**
     * Called when the given <code>player</code> is shutdown.
     * This is the termination state, no calls to the callback methods are expected after this.
     * @param player that is shutdown
     */
    void onShutdown(@NotNull Player player);

    /**
     * Called when the A-B status gets changed for the given <code>player</code>.
     * Normally <code>bPointed</code> should not be true if <code>aPointed</code> is false.
     * @param player for which the A-B status is changed
     * @param aPointed true if the A is pointed, false - otherwise
     * @param bPointed true if the B is pointed, false - otherwise.
     */
    void onABChanged(@NotNull Player player, boolean aPointed, boolean bPointed);

}