package com.frolo.muse.engine;

import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * {@link MeasuredPlayerObserver} measures the callback time of its delegate.
 * Useful for profiling PlayerObservers and checking their performance.
 * NOTE: should only be used in Debug builds, as it consumes some CPU time.
 */
final class MeasuredPlayerObserver implements PlayerObserver {

    private static final String LOG_TAG = "MeasuredPlayerObserver";

    private static final long UNACCEPTABLE_TIME = 400L;
    private static final long CRITICAL_TIME = 50L;

    @NotNull
    static PlayerObserver wrap(@NotNull PlayerObserver delegate) {
        return new MeasuredPlayerObserver(delegate);
    }

    private static String getName(@NotNull PlayerObserver delegate) {
        final String clazzName = delegate.getClass().getName();
        if (clazzName == null || clazzName.isEmpty())
            return "NO_NAME";

        return clazzName;
    }

    private static class Measurement {
        final String mAction;
        final long mStartTime;

        Measurement(String action, long startTime) {
            mAction = action;
            mStartTime = startTime;
        }
    }

    @NotNull
    private final PlayerObserver mDelegate;

    private final String mDelegateName;

    private MeasuredPlayerObserver(@NotNull PlayerObserver delegate) {
        mDelegate = delegate;
        mDelegateName = getName(delegate);
    }

    @NotNull
    private Measurement start(String action) {
        return new Measurement(action, SystemClock.uptimeMillis());
    }

    private void end(@NotNull Measurement m) {
        final long endTime = SystemClock.uptimeMillis();
        handle(m.mAction, endTime - m.mStartTime);
    }

    private boolean isDebuggerConnected() {
        return Debug.isDebuggerConnected();
    }

    private void handle(String eventName, long time) {
        if (isDebuggerConnected()) {
            // There is no point in checking this when debugging
            return;
        }

        if (time >= UNACCEPTABLE_TIME) {
            String msg = mDelegateName + " took " + time + " ms to execute " + eventName + ". It's unacceptable";
            throw new IllegalStateException(msg);
        }

        if (time >= CRITICAL_TIME) {
            Log.e(LOG_TAG, mDelegateName + " took " + time + " ms to execute " + eventName + ". Consider optimization for this");
        } else {
            Log.d(LOG_TAG, mDelegateName + " took " + time + " ms to execute " + eventName);
        }
    }

    @Override
    public void onPrepared(@NotNull Player player, int duration, int progress) {
        final Measurement m = start("onPrepared");
        mDelegate.onPrepared(player, duration, progress);
        end(m);
    }

    @Override
    public void onPlaybackStarted(@NotNull Player player) {
        final Measurement m = start("onPlaybackStarted");
        mDelegate.onPlaybackStarted(player);
        end(m);
    }

    @Override
    public void onPlaybackPaused(@NotNull Player player) {
        final Measurement m = start("onPlaybackPaused");
        mDelegate.onPlaybackPaused(player);
        end(m);
    }

    @Override
    public void onSoughtTo(@NotNull Player player, int position) {
        final Measurement m = start("onSoughtTo");
        mDelegate.onSoughtTo(player, position);
        end(m);
    }

    @Override
    public void onQueueChanged(@NotNull Player player, @NotNull AudioSourceQueue queue) {
        final Measurement m = start("onQueueChanged");
        mDelegate.onQueueChanged(player, queue);
        end(m);
    }

    @Override
    public void onAudioSourceChanged(@NotNull Player player, @Nullable AudioSource item, int positionInQueue) {
        final Measurement m = start("onAudioSourceChanged");
        mDelegate.onAudioSourceChanged(player, item, positionInQueue);
        end(m);
    }

    @Override
    public void onAudioSourceUpdated(@NotNull Player player, @NotNull AudioSource item) {
        final Measurement m = start("onAudioSourceUpdated");
        mDelegate.onAudioSourceUpdated(player, item);
        end(m);
    }

    @Override
    public void onPositionInQueueChanged(@NotNull Player player, int positionInQueue) {
        final Measurement m = start("onPositionInQueueChanged");
        mDelegate.onPositionInQueueChanged(player, positionInQueue);
        end(m);
    }

    @Override
    public void onShuffleModeChanged(@NotNull Player player, int mode) {
        final Measurement m = start("onShuffleModeChanged");
        mDelegate.onShuffleModeChanged(player, mode);
        end(m);
    }

    @Override
    public void onRepeatModeChanged(@NotNull Player player, int mode) {
        final Measurement m = start("onRepeatModeChanged");
        mDelegate.onRepeatModeChanged(player, mode);
        end(m);
    }

    @Override
    public void onShutdown(@NotNull Player player) {
        final Measurement m = start("onShutdown");
        mDelegate.onShutdown(player);
        end(m);
    }

    @Override
    public void onABChanged(@NotNull Player player, boolean aPointed, boolean bPointed) {
        final Measurement m = start("onABChanged");
        mDelegate.onABChanged(player, aPointed, bPointed);
        end(m);
    }

    @Override
    public void onPlaybackSpeedChanged(@NotNull Player player, float speed) {
        final Measurement m = start("onPlaybackSpeedChanged");
        mDelegate.onPlaybackSpeedChanged(player, speed);
        end(m);
    }

    @Override
    public void onPlaybackPitchChanged(@NotNull Player player, float pitch) {
        final Measurement m = start("onPlaybackPitchChanged");
        mDelegate.onPlaybackPitchChanged(player, pitch);
        end(m);
    }

    @Override
    public void onInternalErrorOccurred(@NotNull Player player, @NotNull Throwable error) {
        final Measurement m = start("onInternalErrorOccurred");
        mDelegate.onInternalErrorOccurred(player, error);
        end(m);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof MeasuredPlayerObserver)) return false;

        final MeasuredPlayerObserver other = (MeasuredPlayerObserver) o;
        return Objects.equals(mDelegate, other.mDelegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDelegate, mDelegateName);
    }

}
