package com.frolo.muse.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Simple implementation of {@link PlayerObserver} that does nothing in callback methods.
 * Useful if the client only wants to implement some particular methods in {@link PlayerObserver}.
 */
public abstract class SimplePlayerObserver implements PlayerObserver {

    @Override
    public void onPrepared(@NotNull Player player, int duration, int progress) {
    }

    @Override
    public void onPlaybackStarted(@NotNull Player player) {
    }

    @Override
    public void onPlaybackPaused(@NotNull Player player) {
    }

    @Override
    public void onSoughtTo(@NotNull Player player, int position) {
    }

    @Override
    public void onQueueChanged(@NotNull Player player, @NotNull AudioSourceQueue queue) {
    }

    @Override
    public void onAudioSourceChanged(@NotNull Player player, @Nullable AudioSource item, int positionInQueue) {
    }

    @Override
    public void onAudioSourceUpdated(@NotNull Player player, @NotNull AudioSource item) {
    }

    @Override
    public void onPositionInQueueChanged(@NotNull Player player, int positionInQueue) {
    }

    @Override
    public void onShuffleModeChanged(@NotNull Player player, @Player.ShuffleMode int mode) {
    }

    @Override
    public void onRepeatModeChanged(@NotNull Player player, @Player.RepeatMode int mode) {
    }

    @Override
    public void onShutdown(@NotNull Player player) {
    }

    @Override
    public void onABChanged(@NotNull Player player, boolean aPointed, boolean bPointed) {
    }

    @Override
    public void onPlaybackSpeedChanged(@NotNull Player player, float speed) {
    }

    @Override
    public void onPlaybackPitchChanged(@NotNull Player player, float pitch) {
    }

    @Override
    public void onInternalErrorOccurred(@NotNull Player player, @NotNull Throwable error) {
    }

}