package com.frolo.muse.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @return a new instance of {@link PlayerObserverRegistry}
     */
    static PlayerObserverRegistry create(@NotNull Context context, @NotNull Player player) {
        return new PlayerObserverRegistry(context, player);
    }

    private static final int ARG_NOTHING = 0;

    private class DispatcherHandler extends Handler {

        static final int MSG_PREPARED = 0;
        static final int MSG_PLAYBACK_STARTED = 1;
        static final int MSG_PLAYBACK_PAUSED = 2;
        static final int MSG_SOUGHT_TO = 3;
        static final int MSG_QUEUE_CHANGED = 4;
        static final int MSG_CURRENT_ITEM_CHANGED = 5;
        static final int MSG_SHUFFLE_MODE_CHANGED = 6;
        static final int MSG_REPEAT_MODE_CHANGED = 7;
        static final int MSG_SHUTDOWN = 8;
        static final int MSG_AB_CHANGED = 9;

        DispatcherHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {

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
            }
        }
    }

    private final CopyOnWriteArraySet<PlayerObserver> mObservers = new CopyOnWriteArraySet<>();

    private final DispatcherHandler mHandler;

    @NotNull
    private final Player mPlayer;

    private PlayerObserverRegistry(@NotNull Context context, @NotNull Player player) {
        mPlayer = player;
        mHandler = new DispatcherHandler(context.getMainLooper());
    }

    @NotNull
    Player getPlayer() {
        return mPlayer;
    }

    void register(PlayerObserver observer) {
        if (observer == null) {
            return;
        }

        mObservers.add(MeasuredPlayerObserver.wrap(observer));
    }

    void unregister(PlayerObserver observer) {
        if (observer == null) {
            return;
        }

        mObservers.remove(MeasuredPlayerObserver.wrap(observer));
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
    private void dispatch(@NotNull Message message) {
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

    synchronized void dispatchQueueChanged(@NotNull AudioSourceQueue queue) {
        mHandler.removeMessages(DispatcherHandler.MSG_QUEUE_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_QUEUE_CHANGED, queue);
        dispatch(message);
    }

    synchronized void dispatchAudioSourceChanged(@Nullable AudioSource item, int positionInQueue) {
        mHandler.removeMessages(DispatcherHandler.MSG_CURRENT_ITEM_CHANGED);

        final Message message =
                mHandler.obtainMessage(DispatcherHandler.MSG_CURRENT_ITEM_CHANGED, positionInQueue, ARG_NOTHING, item);
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

}
