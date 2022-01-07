package com.frolo.player;

import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @NonNull
    static PlayerObserver wrap(@NonNull PlayerObserver delegate) {
        return new MeasuredPlayerObserver(delegate);
    }

    private static String getName(@NonNull PlayerObserver delegate) {
        final String clazzName = delegate.getClass().getName();
        if (clazzName == null || clazzName.isEmpty()) {
            return "NO_NAME";
        }

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

    @NonNull
    private final PlayerObserver mDelegate;

    private final String mDelegateName;

    private MeasuredPlayerObserver(@NonNull PlayerObserver delegate) {
        mDelegate = delegate;
        mDelegateName = getName(delegate);
    }

    @NonNull
    private Measurement start(String action) {
        return new Measurement(action, SystemClock.uptimeMillis());
    }

    private void end(@NonNull Measurement m) {
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
    public void onPrepared(@NonNull Player player, int duration, int progress) {
        final Measurement m = start("onPrepared");
        mDelegate.onPrepared(player, duration, progress);
        end(m);
    }

    @Override
    public void onPlaybackStarted(@NonNull Player player) {
        final Measurement m = start("onPlaybackStarted");
        mDelegate.onPlaybackStarted(player);
        end(m);
    }

    @Override
    public void onPlaybackPaused(@NonNull Player player) {
        final Measurement m = start("onPlaybackPaused");
        mDelegate.onPlaybackPaused(player);
        end(m);
    }

    @Override
    public void onSoughtTo(@NonNull Player player, int position) {
        final Measurement m = start("onSoughtTo");
        mDelegate.onSoughtTo(player, position);
        end(m);
    }

    @Override
    public void onQueueChanged(@NonNull Player player, @NonNull AudioSourceQueue queue) {
        final Measurement m = start("onQueueChanged");
        mDelegate.onQueueChanged(player, queue);
        end(m);
    }

    @Override
    public void onAudioSourceChanged(@NonNull Player player, @Nullable AudioSource item, int positionInQueue) {
        final Measurement m = start("onAudioSourceChanged");
        mDelegate.onAudioSourceChanged(player, item, positionInQueue);
        end(m);
    }

    @Override
    public void onAudioSourceUpdated(@NonNull Player player, @NonNull AudioSource item) {
        final Measurement m = start("onAudioSourceUpdated");
        mDelegate.onAudioSourceUpdated(player, item);
        end(m);
    }

    @Override
    public void onPositionInQueueChanged(@NonNull Player player, int positionInQueue) {
        final Measurement m = start("onPositionInQueueChanged");
        mDelegate.onPositionInQueueChanged(player, positionInQueue);
        end(m);
    }

    @Override
    public void onShuffleModeChanged(@NonNull Player player, int mode) {
        final Measurement m = start("onShuffleModeChanged");
        mDelegate.onShuffleModeChanged(player, mode);
        end(m);
    }

    @Override
    public void onRepeatModeChanged(@NonNull Player player, int mode) {
        final Measurement m = start("onRepeatModeChanged");
        mDelegate.onRepeatModeChanged(player, mode);
        end(m);
    }

    @Override
    public void onShutdown(@NonNull Player player) {
        final Measurement m = start("onShutdown");
        mDelegate.onShutdown(player);
        end(m);
    }

    @Override
    public void onABChanged(@NonNull Player player, boolean aPointed, boolean bPointed) {
        final Measurement m = start("onABChanged");
        mDelegate.onABChanged(player, aPointed, bPointed);
        end(m);
    }

    @Override
    public void onPlaybackSpeedChanged(@NonNull Player player, float speed) {
        final Measurement m = start("onPlaybackSpeedChanged");
        mDelegate.onPlaybackSpeedChanged(player, speed);
        end(m);
    }

    @Override
    public void onPlaybackPitchChanged(@NonNull Player player, float pitch) {
        final Measurement m = start("onPlaybackPitchChanged");
        mDelegate.onPlaybackPitchChanged(player, pitch);
        end(m);
    }

    @Override
    public void onInternalErrorOccurred(@NonNull Player player, @NonNull Throwable error) {
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
