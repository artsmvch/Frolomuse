package com.frolo.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * Thread-safe registry implementation for {@link PlayerObserver}.
 * All {@link PlayerObserver} callback methods are called on the main thread.
 * Observers can be registered using {@link PlayerObserverRegistry#register(PlayerObserver)} method
 * and unregistered using {@link PlayerObserverRegistry#unregister(PlayerObserver)} method.
 */
final class PlayerObserverRegistry {

    /**
     * Factory method that creates a new instance of {@link PlayerObserverRegistry}.
     * @param context to create a handler for events
     * @param player the observed player
     * @param debug debug mode
     * @return a new instance of {@link PlayerObserverRegistry}
     */
    static PlayerObserverRegistry create(@NonNull Context context, @NonNull Player player, boolean debug) {
        return new PlayerObserverRegistry(context, player, debug);
    }

    private static final int ARG_NOTHING = 0;

    private class DispatcherHandler extends Handler {

        static final int MSG_PREPARED = 0;
        static final int MSG_PLAYBACK_STARTED = 1;
        static final int MSG_PLAYBACK_PAUSED = 2;
        static final int MSG_SOUGHT_TO = 3;
        static final int MSG_QUEUE_CHANGED = 4;
        static final int MSG_CURRENT_ITEM_CHANGED = 5;
        static final int MSG_CURRENT_POSITION_CHANGED = 6;
        static final int MSG_SHUFFLE_MODE_CHANGED = 7;
        static final int MSG_REPEAT_MODE_CHANGED = 8;
        static final int MSG_SHUTDOWN = 9;
        static final int MSG_AB_CHANGED = 10;
        static final int MSG_SPEED_CHANGED = 11;
        static final int MSG_PITCH_CHANGED = 12;
        static final int MSG_INTERNAL_ERROR_OCCURRED = 13;
        static final int MSG_CURRENT_ITEM_UPDATED = 14;

        DispatcherHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case MSG_PREPARED: {
                    final int duration = msg.arg1;
                    final int progress = msg.arg2;
                    for (PlayerObserver observer : mObservers) {
                        observer.onPrepared(getPlayer(), duration, progress);
                    }
                    break;
                }

                case MSG_PLAYBACK_STARTED: {
                    for (PlayerObserver observer : mObservers) {
                        observer.onPlaybackStarted(getPlayer());
                    }
                    break;
                }

                case MSG_PLAYBACK_PAUSED: {
                    for (PlayerObserver observer : mObservers) {
                        observer.onPlaybackPaused(getPlayer());
                    }
                    break;
                }

                case MSG_SOUGHT_TO: {
                    final int position = msg.arg1;
                    for (PlayerObserver observer : mObservers) {
                        observer.onSoughtTo(getPlayer(), position);
                    }
                    break;
                }

                case MSG_QUEUE_CHANGED: {
                    final AudioSourceQueue queue = (AudioSourceQueue) msg.obj;
                    for (PlayerObserver observer : mObservers) {
                        observer.onQueueChanged(getPlayer(), queue);
                    }
                    break;
                }

                case MSG_CURRENT_ITEM_CHANGED: {
                    final AudioSource item = (AudioSource) msg.obj;
                    final int positionInQueue = msg.arg1;
                    for (PlayerObserver observer : mObservers) {
                        observer.onAudioSourceChanged(getPlayer(), item, positionInQueue);
                    }
                    break;
                }

                case MSG_CURRENT_POSITION_CHANGED: {
                    final int positionInQueue = msg.arg1;
                    for (PlayerObserver observer : mObservers) {
                        observer.onPositionInQueueChanged(getPlayer(), positionInQueue);
                    }
                    break;
                }

                case MSG_SHUFFLE_MODE_CHANGED: {
                    final int shuffleMode = msg.arg1;
                    for (PlayerObserver observer : mObservers) {
                        observer.onShuffleModeChanged(getPlayer(), shuffleMode);
                    }
                    break;
                }

                case MSG_REPEAT_MODE_CHANGED: {
                    final int repeatMode = msg.arg1;
                    for (PlayerObserver observer : mObservers) {
                        observer.onRepeatModeChanged(getPlayer(), repeatMode);
                    }
                    break;
                }

                case MSG_SHUTDOWN: {
                    for (PlayerObserver observer : mObservers) {
                        observer.onShutdown(getPlayer());
                    }
                    // Automatic removal of all observers after the shutdown
                    mObservers.clear();
                    break;
                }

                case MSG_AB_CHANGED: {
                    final boolean aPointed = msg.arg1 == 0;
                    final boolean bPointed = msg.arg2 == 0;
                    for (PlayerObserver observer : mObservers) {
                        observer.onABChanged(getPlayer(), aPointed, bPointed);
                    }
                    break;
                }

                case MSG_SPEED_CHANGED: {
                    final float speed = (Float) msg.obj;
                    for (PlayerObserver observer : mObservers) {
                        observer.onPlaybackSpeedChanged(getPlayer(), speed);
                    }
                    break;
                }

                case MSG_PITCH_CHANGED: {
                    final float pitch = (Float) msg.obj;
                    for (PlayerObserver observer : mObservers) {
                        observer.onPlaybackPitchChanged(getPlayer(), pitch);
                    }
                    break;
                }

                case MSG_INTERNAL_ERROR_OCCURRED: {
                    final Throwable error = (Throwable) msg.obj;
                    for (PlayerObserver observer : mObservers) {
                        observer.onInternalErrorOccurred(getPlayer(), error);
                    }
                    break;
                }

                case MSG_CURRENT_ITEM_UPDATED: {
                    final AudioSource item = (AudioSource) msg.obj;
                    for (PlayerObserver observer : mObservers) {
                        observer.onAudioSourceUpdated(getPlayer(), item);
                    }
                    break;
                }
            }
        }
    }

    private final CopyOnWriteArraySet<PlayerObserver> mObservers = new CopyOnWriteArraySet<>();

    private final DispatcherHandler mHandler;

    @NonNull
    private final Player mPlayer;

    private final boolean mDebug;

    private PlayerObserverRegistry(@NonNull Context context, @NonNull Player player, boolean debug) {
        mPlayer = player;
        mDebug = debug;
        mHandler = new DispatcherHandler(context.getMainLooper());
    }

    @NonNull
    Player getPlayer() {
        return mPlayer;
    }

    private PlayerObserver wrapIfNeeded(PlayerObserver observer) {
        if (mDebug) {
            return MeasuredPlayerObserver.wrap(observer);
        } else {
            return observer;
        }
    }

    void register(PlayerObserver observer) {
        if (observer == null) {
            return;
        }

        mObservers.add(wrapIfNeeded(observer));
    }

    void registerAll(Collection<PlayerObserver> observers) {
        if (observers == null) {
            return;
        }

        for (PlayerObserver observer : observers) {
            if (observer != null) {
                mObservers.add(wrapIfNeeded(observer));
            }
        }
    }

    void unregister(PlayerObserver observer) {
        if (observer == null) {
            return;
        }

        mObservers.remove(wrapIfNeeded(observer));
    }

    void clear() {
        mObservers.clear();
    }

    /**
     * Dispatches the given <code>message</code> to {@link PlayerObserverRegistry#mHandler}.
     * If the call occurs on the handler's thread, then the message is handled immediately.
     * Otherwise ,the message is sent to the target asynchronously.
     *
     * @param message to dispatch
     */
    private void dispatch(@NonNull Message message) {
        // TODO: leave as is?
        message.sendToTarget();
//        if (mHandler.getLooper().isCurrentThread()) {
//            mHandler.handleMessage(message);
//        } else {
//            message.sendToTarget();
//        }
    }

    synchronized void dispatchPrepared(int duration, int progress) {
        mHandler.removeMessages(DispatcherHandler.MSG_PREPARED);
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_STARTED);
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_PAUSED);

        final Message message = mHandler.obtainMessage(DispatcherHandler.MSG_PREPARED, duration, progress);
        dispatch(message);
    }

    synchronized void dispatchPlaybackStarted() {
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_STARTED);
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_PAUSED);

        final Message message = mHandler.obtainMessage(DispatcherHandler.MSG_PLAYBACK_STARTED);
        dispatch(message);
    }

    synchronized void dispatchPlaybackPaused() {
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_STARTED);
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_PAUSED);

        final Message message = mHandler.obtainMessage(DispatcherHandler.MSG_PLAYBACK_PAUSED);
        dispatch(message);
    }

    synchronized void dispatchSoughtTo(int position) {
        mHandler.removeMessages(DispatcherHandler.MSG_SOUGHT_TO);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_SOUGHT_TO, position, ARG_NOTHING);
        dispatch(message);
    }

    synchronized void dispatchQueueChanged(@NonNull AudioSourceQueue queue) {
        mHandler.removeMessages(DispatcherHandler.MSG_QUEUE_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_QUEUE_CHANGED, queue);
        dispatch(message);
    }

    synchronized void dispatchAudioSourceChanged(@Nullable AudioSource item, int positionInQueue) {
        mHandler.removeMessages(DispatcherHandler.MSG_CURRENT_ITEM_CHANGED);
        mHandler.removeMessages(DispatcherHandler.MSG_CURRENT_ITEM_UPDATED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_CURRENT_ITEM_CHANGED, positionInQueue, ARG_NOTHING, item);
        dispatch(message);
    }

    synchronized void dispatchAudioSourceUpdated(@NonNull AudioSource item) {
        mHandler.removeMessages(DispatcherHandler.MSG_CURRENT_ITEM_UPDATED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_CURRENT_ITEM_UPDATED, item);
        dispatch(message);
    }

    synchronized void dispatchPositionInQueueChanged(int positionInQueue) {
        mHandler.removeMessages(DispatcherHandler.MSG_CURRENT_POSITION_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_CURRENT_POSITION_CHANGED, positionInQueue, ARG_NOTHING);
        dispatch(message);
    }

    synchronized void dispatchShuffleModeChanged(int mode) {
        mHandler.removeMessages(DispatcherHandler.MSG_SHUFFLE_MODE_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_SHUFFLE_MODE_CHANGED, mode, ARG_NOTHING);
        dispatch(message);
    }

    synchronized void dispatchRepeatModeChanged(int mode) {
        mHandler.removeMessages(DispatcherHandler.MSG_REPEAT_MODE_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_REPEAT_MODE_CHANGED, mode, ARG_NOTHING);
        dispatch(message);
    }

    synchronized void dispatchShutdown() {
        mHandler.removeMessages(DispatcherHandler.MSG_PREPARED);
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_STARTED);
        mHandler.removeMessages(DispatcherHandler.MSG_PLAYBACK_PAUSED);
        mHandler.removeMessages(DispatcherHandler.MSG_SOUGHT_TO);
        mHandler.removeMessages(DispatcherHandler.MSG_QUEUE_CHANGED);
        mHandler.removeMessages(DispatcherHandler.MSG_CURRENT_ITEM_CHANGED);
        mHandler.removeMessages(DispatcherHandler.MSG_SHUFFLE_MODE_CHANGED);
        mHandler.removeMessages(DispatcherHandler.MSG_REPEAT_MODE_CHANGED);
        mHandler.removeMessages(DispatcherHandler.MSG_SHUTDOWN);
        mHandler.removeMessages(DispatcherHandler.MSG_AB_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_SHUTDOWN);
        dispatch(message);
    }

    synchronized void dispatchABChanged(boolean aPointed, boolean bPointed) {
        mHandler.removeMessages(DispatcherHandler.MSG_AB_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_AB_CHANGED, aPointed ? 0 : 1, bPointed ? 0 : 1);
        dispatch(message);
    }

    synchronized void dispatchSpeedChanged(float speed) {
        mHandler.removeMessages(DispatcherHandler.MSG_SPEED_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_SPEED_CHANGED, Float.valueOf(speed));
        dispatch(message);
    }

    synchronized void dispatchPitchChanged(float pitch) {
        mHandler.removeMessages(DispatcherHandler.MSG_PITCH_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_PITCH_CHANGED, Float.valueOf(pitch));
        dispatch(message);
    }

    synchronized void dispatchInternalErrorOccurred(@NonNull Throwable error) {
        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_INTERNAL_ERROR_OCCURRED, error);
        dispatch(message);
    }

    synchronized void post(@NonNull Runnable r) {
        mHandler.post(r);
    }

}
