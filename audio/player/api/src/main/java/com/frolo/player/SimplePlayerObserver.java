package com.frolo.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Simple implementation of {@link PlayerObserver} that does nothing in callback methods.
 * Useful if the client only wants to implement some particular methods in {@link PlayerObserver}.
 */
public abstract class SimplePlayerObserver implements PlayerObserver {

    @Override
    public void onPrepared(@NonNull Player player, int duration, int progress) {
    }

    @Override
    public void onPlaybackStarted(@NonNull Player player) {
    }

    @Override
    public void onPlaybackPaused(@NonNull Player player) {
    }

    @Override
    public void onSoughtTo(@NonNull Player player, int position) {
    }

    @Override
    public void onQueueChanged(@NonNull Player player, @NonNull AudioSourceQueue queue) {
    }

    @Override
    public void onAudioSourceChanged(@NonNull Player player, @Nullable AudioSource item, int positionInQueue) {
    }

    @Override
    public void onAudioSourceUpdated(@NonNull Player player, @NonNull AudioSource item) {
    }

    @Override
    public void onPositionInQueueChanged(@NonNull Player player, int positionInQueue) {
    }

    @Override
    public void onShuffleModeChanged(@NonNull Player player, @Player.ShuffleMode int mode) {
    }

    @Override
    public void onRepeatModeChanged(@NonNull Player player, @Player.RepeatMode int mode) {
    }

    @Override
    public void onShutdown(@NonNull Player player) {
    }

    @Override
    public void onABChanged(@NonNull Player player, boolean aPointed, boolean bPointed) {
    }

    @Override
    public void onPlaybackSpeedChanged(@NonNull Player player, float speed) {
    }

    @Override
    public void onPlaybackPitchChanged(@NonNull Player player, float pitch) {
    }

    @Override
    public void onInternalErrorOccurred(@NonNull Player player, @NonNull Throwable error) {
    }

}