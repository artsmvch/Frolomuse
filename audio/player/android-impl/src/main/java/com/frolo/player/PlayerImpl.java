package com.frolo.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.player.data.AudioSources;
import com.frolo.player.data.MediaStoreRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


// TODO: fix locks: Engine must be guarded everywhere
// TODO: try using VolumeShaper for playback fading
/**
 * Android-specific thread-safe implementation of {@link Player}.
 * Methods prefixed with '_' are of type Runnable and must be processed with
 * {@link PlayerImpl#processEngineTask(Runnable, Object, boolean)} or {@link PlayerImpl#processEngineTask(Runnable)} methods.
 * All public methods are non-blocking and can be called from any thread.
 * Events are dispatched on the main thread using {@link PlayerObserverRegistry}.
 * The implementation uses {@link MediaPlayer} under the hood.
 */
public final class PlayerImpl implements Player, AdvancedPlaybackParams {

    private static final int NO_AUDIO_SESSION = 0;

    private static final int NO_POSITION_IN_QUEUE = -1;

    /**
     * Token for engine tasks that are responsible for skipping the playing position to any other.
     * Such tasks include: skipToPrevious, skipToNext, skipToPosition, skipToItem.
     */
    private static final Object TOKEN_SKIP_TO = new Object();

    /**
     * This value describes how often the volume should be adjusted to fade in/out smoothly.
     */
    private static final long VOLUME_ADJUSTMENT_INTERVAL = 50L;

    private static final MathUtil.Range SPEED_RANGE = new MathUtil.Range(0f, 2f);
    private static final MathUtil.Range PITCH_RANGE = new MathUtil.Range(0f, 2f);

    public static class PlayerException extends RuntimeException {
        public PlayerException(String message) {
            super(message);
        }

        public PlayerException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Builder for creating PlayerImpl instances.
     */
    public static final class Builder {
        private final Context mContext;
        @Nullable
        private MediaPlayerHook mMediaPlayerHook;
        private AudioFocusRequester.Factory mAudioFocusRequesterFactory;
        private boolean mDebug = false;
        private PlayerJournal mJournal;
        private boolean mUseWakeLocks;
        @RepeatMode
        private int mRepeatMode;
        @ShuffleMode
        private int mShuffleMode;
        private PlaybackFadingStrategy mPlaybackFadingStrategy;
        private final List<PlayerObserver> mObservers = new ArrayList<>();

        private Builder(@NonNull Context context) {
            mContext = context;
        }

        private Builder self() {
            return this;
        }

        public Builder setMediaPlayerHook(@Nullable MediaPlayerHook hook) {
            mMediaPlayerHook = hook;
            return self();
        }

        public Builder setDebug(boolean debug) {
            mDebug = debug;
            return self();
        }

        public Builder setAudioFocusRequesterFactory(@NonNull AudioFocusRequester.Factory factory) {
            mAudioFocusRequesterFactory = factory;
            return self();
        }

        public Builder setPlayerJournal(@Nullable PlayerJournal journal) {
            mJournal = journal;
            return self();
        }

        public Builder setUseWakeLocks(boolean useWakeLocks) {
            mUseWakeLocks = useWakeLocks;
            return self();
        }

        public Builder setRepeatMode(@RepeatMode int mode) {
            mRepeatMode = mode;
            return self();
        }

        public Builder setShuffleMode(@ShuffleMode int mode) {
            mShuffleMode = mode;
            return self();
        }

        public Builder setPlaybackFadingStrategy(@Nullable PlaybackFadingStrategy strategy) {
            mPlaybackFadingStrategy = strategy;
            return self();
        }

        public Builder addObserver(@NonNull PlayerObserver observer) {
            mObservers.add(observer);
            return self();
        }

        @NonNull
        public PlayerImpl build() {
            return new PlayerImpl(this);
        }
    }

    @NonNull
    public static Builder newBuilder(@NonNull Context context) {
        return new Builder(context);
    }

    /**
     * Wrapper for engine tasks. This does additional work,
     * including checking the execution thread and the shutdown status of the player.
     */
    private final class EngineTaskWrapper implements Runnable {

        final Runnable mTask;

        EngineTaskWrapper(@NonNull Runnable task) {
            mTask = task;
        }

        @Override
        public void run() {
            // At first, making sure that we're on the engine thread
            assertEngineThread();
            // Then, we need to check if the player is shutdown
            if (isShutdown()) return;
            // Finally, executing the task
            try {
                mTask.run();
            } catch (Throwable error) {
                mObserverRegistry.dispatchInternalErrorOccurred(error);
                // Oops, need to do a hard reset
                if (mDebug) {
                    failOnEventThread(error);
                }
                performHardReset(error);
            }
        }
    }

    /**
     * Creates a new handler that will work on a background thread.
     * The method is a little bit blocking, it's better not to call it on the main thread.
     * @return a new engine handler
     */
    @NonNull
    static Handler createEngineHandler() {
        final HandlerThread thread = new HandlerThread("PlayerEngine");
        thread.start();
        return new Handler(thread.getLooper());
    }

    @NonNull
    static Handler createEventHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @NonNull
    private static String info(@Nullable AudioSourceQueue queue) {
        if (queue == null) {
            return "Queue[null]";
        }

        return "Queue[name=" + queue + ", length=" + queue.getLength() + "]";
    }

    @NonNull
    private static String info(@Nullable AudioSource audioSource) {
        if (audioSource == null) {
            return "AudioSource[null]";
        }

        return "AudioSource[source=" + audioSource.getSource() + "]";
    }

    /**
     * Checks if the error described by <code>what</code> and <code>extra</code> is critical for {@link MediaPlayer}.
     * If the error is critical, then the media player should no longer be used.
     * The last action to take is release the instance and let the GC collect it.
     * @param what kind of error
     * @param extra extra error info
     * @return true if the error is critical and the media player should be released, false - otherwise
     */
    private static boolean isCriticalEngineError(int what, /* unused */ int extra) {
        return what == MediaPlayer.MEDIA_ERROR_SERVER_DIED;
    }

    private final boolean mDebug;

    // Guard for mEngine
    private final Object mEngineLock = new Object();

    // Flag that indicates whether the player is shutdown
    private final AtomicBoolean mShutdown = new AtomicBoolean(false);

    // Context
    private final Context mContext;

    // Handlers
    private final Handler mEngineHandler;
    private final Object mEngineTasksLock = new Object();

    // Wake locks
    private final boolean mUseWakeLocks;

    // Player journal
    @NonNull
    private final PlayerJournal mPlayerJournal;

    // Audio focus requester factory: the requester is created lazily
    @NonNull
    private final AudioFocusRequester.Factory mAudioFocusRequesterFactory;
    @GuardedBy("mAudioFocusRequesterFactory")
    private AudioFocusRequester mAudioFocusRequester = null;

    // Media player hook
    @NonNull
    private final MediaPlayerHook mMediaPlayerHook;

    // Observer Registry
    @NonNull
    private final PlayerObserverRegistry mObserverRegistry;

    // Engine
    @GuardedBy("mEngineLock")
    @Nullable
    private volatile MediaPlayer mEngine;

    /**
     * Error handler for {@link MediaPlayer}.
     */
    private final MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            processEngineTask(_handleEngineError(mp, what, extra));
            // We always should return true,
            // because, returning false stops the playback.
            return true;
        }
    };

    private final Object mOnCompletionWaiter = new Object();

    /**
     * Completion handler for {@link MediaPlayer}.
     */
    private final MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            processEngineTask(_skipToNext(false));

            // Notifying all threads that the playback is complete
            notifyPlaybackCompleted();
        }
    };

    // Internal state
    @Nullable
    private volatile AudioSourceQueue mOriginQueue = null;
    @NonNull
    private volatile AudioSourceQueue mCurrentQueue = AudioSourceQueue.empty();
    @Nullable
    private volatile AudioSource mCurrentItem = null;
    private volatile int mCurrentPositionInQueue = NO_POSITION_IN_QUEUE;

    private volatile boolean mIsPreparedFlag = false;
    private volatile boolean mIsPlayingFlag = false;

    @RepeatMode
    private volatile int mRepeatMode = Player.REPEAT_OFF;
    @ShuffleMode
    private volatile int mShuffleMode = Player.SHUFFLE_OFF;

    // AB Engine
    @NonNull
    private final ABEngine mABEngine = new ABEngine();

    // Playback Fading
    @Nullable
    private volatile PlaybackFadingStrategy mPlaybackFadingStrategy;
    private volatile Timer mPlaybackFadingTimer;

    // Playback Params
    private volatile boolean mIsPlaybackSpeedPersisted = false;
    private volatile float mPlaybackSpeed = SPEED_NORMAL;
    private volatile boolean mIsPlaybackPitchPersisted = false;
    private volatile float mPlaybackPitch = PITCH_NORMAL;

    private PlayerImpl(@NonNull Builder builder) {
        mContext = builder.mContext;

        mDebug = builder.mDebug;

        mEngineHandler = createEngineHandler();

        mUseWakeLocks = builder.mUseWakeLocks;

        mPlayerJournal = builder.mJournal != null ? builder.mJournal : PlayerJournal.EMPTY;

        mAudioFocusRequesterFactory = builder.mAudioFocusRequesterFactory != null
                ? builder.mAudioFocusRequesterFactory
                : AudioFocusRequesterImpl.obtainFactory(builder.mContext);

        mMediaPlayerHook = builder.mMediaPlayerHook != null
            ? builder.mMediaPlayerHook
            : new MediaPlayerHookStub();

        mObserverRegistry = PlayerObserverRegistry.create(builder.mContext, this, builder.mDebug);
        mObserverRegistry.registerAll(builder.mObservers);

        mRepeatMode = builder.mRepeatMode;
        mShuffleMode = builder.mShuffleMode;

        mPlaybackFadingStrategy = builder.mPlaybackFadingStrategy;
        startPlaybackFadingTimer();
    }

    /**
     * Checks if the execution happens on the engine thread.
     * If not, then {@link IllegalStateException} is thrown.
     * NOTE: this only works for debug builds.
     */
    private void assertEngineThread() {
        if (mDebug) {
            final boolean isOnEngineThread = mEngineHandler.getLooper().isCurrentThread();
            if (!isOnEngineThread) {
                throw new IllegalStateException("Called not on engine thread");
            }
        }
    }

    /**
     * Reports the given <code>error</code> that occurred in this player.
     * @param error to report
     */
    private void report(Throwable error) {
        final PlayerException playerException = error instanceof PlayerException
                ? (PlayerException) error
                : new PlayerException(error);
        mObserverRegistry.dispatchInternalErrorOccurred(playerException);
    }

    @NonNull
    private AudioFocusRequester getAudioFocusRequester() {
        synchronized (mAudioFocusRequesterFactory) {
            AudioFocusRequester requester = mAudioFocusRequester;
            if (requester == null) {
                requester = mAudioFocusRequesterFactory.create(this);
                mAudioFocusRequester = requester;
            }
            return requester;
        }
    }

    /**
     * Posts the given <code>task</code> to the engine thread through the appropriate handler.
     * The task can be associated with <code>token</code>. The token may be null.
     * If <code>cancelPrevious</code> is true, then all pending tasks associated with the <code>token</code> will be cancelled.
     * To cancel ALL pending tasks the token should be <code>null</code>.
     * @param task to process
     * @param token to associate the task with
     * @param cancelPrevious if true, then all pending tasks associated with the given token will be cancelled
     */
    private void processEngineTask(@NonNull Runnable task, @Nullable Object token, boolean cancelPrevious) {
        synchronized (mEngineTasksLock) {
            if (cancelPrevious) {
                // Cancelling all the pending tasks associated with the token.
                // If the token is null, then all tasks will be cancelled.
                mEngineHandler.removeCallbacksAndMessages(token);
            }

            final Runnable wrapper = new EngineTaskWrapper(task);
            mEngineHandler.postAtTime(wrapper, token, SystemClock.uptimeMillis());
        }
    }

    /**
     * Posts the given <code>task</code> to the engine thread through the appropriate handler.
     * This does not cancel any pending task, it only queues the given task for processing.
     * @param task to process
     */
    private void processEngineTask(@NonNull Runnable task) {
        processEngineTask(task, null, false);
    }

    /**
     * Cancels all pending engine tasks.
     */
    private void cancelAllEngineTasks() {
        synchronized (mEngineTasksLock) {
            mEngineHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Executes <code>action</code> on the engine thread.
     * Posting is async in the sense that if the call occurs on the engine thread,
     * the execution will not be performed immediately, but in the near future.
     * @param action to execute on the engine thread
     * @deprecated the action can be cancelled by calling {@link PlayerImpl#processEngineTask(Runnable, Object, boolean)} method
     */
    @Deprecated
    public void postOnEngineThread(@NonNull Runnable action) {
        processEngineTask(action);
    }

    /**
     * Executes <code>action</code> on the event thread.
     * The execution can be delayed until all pending and potential event tasks are performed.
     * @param action to execute on the event thread
     * @param delayed true if the action should executed at the very end after all pending and potential event tasks
     * @deprecated if delayed, the action can be cancelled by calling {@link PlayerImpl#processEngineTask(Runnable, Object, boolean)} method
     */
    @Deprecated
    public void postOnEventThread(@NonNull Runnable action, boolean delayed) {
        if (delayed) {
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    mObserverRegistry.post(action);
                }
            };
            processEngineTask(task);
        } else {
            mObserverRegistry.post(action);
        }
    }

    /**
     * Awaits until the playback is complete, i.e. the playback reaches the end.
     * NOTE: be careful with calling this method: it waits even if the engine is not playing at the moment.
     * This method should only be used for testing.
     */
    final void awaitPlaybackCompletion() {
        synchronized (mOnCompletionWaiter) {
            try {
                mOnCompletionWaiter.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void notifyPlaybackCompleted() {
        synchronized (mOnCompletionWaiter) {
            mOnCompletionWaiter.notifyAll();
        }
    }

    /**
     * Tries to request audio focus.
     * Returns true if the focus is granted and the player can start playing, false - otherwise.
     * @return true if the audio focus is granted, false - otherwise.
     */
    private boolean tryRequestAudioFocus() {
        try {
            boolean result = getAudioFocusRequester().requestAudioFocus();
            mPlayerJournal.logMessage("Request audio focus: result=" + result);
            return result;
        } catch (Throwable error) {
            report(error);
            mPlayerJournal.logError("Failed to request audio focus", error);
            return true;
        }
    }

    private Player self() {
        return this;
    }

    /**
     * Returns true if {@link Player#shutdown()} was called on this instance.
     * If so, then the instance can no longer be used and any method calls should be ignored.
     * @return true if the player is shutdown, false - otherwise
     */
    @Override
    public boolean isShutdown() {
        return mShutdown.get();
    }

    @NonNull
    protected final Context getContext() {
        return mContext;
    }

    /**
     * Throws the given <code>error</code> on the event thread. Typically, the event thread
     * is the main thread of the application. This method can be used for debugging, for example
     * to signal an exception that cannot be handled properly, thus it would better to crash the app.
     * @param error to throw on the event thread.
     */
    private void failOnEventThread(Throwable error) {
        final Runnable action = new Runnable() {
            @Override
            public void run() {
                if (error instanceof RuntimeException) {
                    throw (RuntimeException) error;
                } else {
                    throw new RuntimeException(error);
                }
            }
        };
        mObserverRegistry.post(action);
    }

    /**
     * Performs a hard reset on this instance. Be careful, this should only be done in an emergency.
     * NOTE: At the moment, this method does not notify observers about the reset.
     */
    final void performHardReset(Throwable cause) {
        synchronized (mEngineLock) {

            mPlayerJournal.logError("Perform hard reset", cause);

            //throw new UnsupportedOperationException("Not implemented yet!");

            try {
                MediaPlayer engine = mEngine;
                if (engine != null) {
                    engine.release();
                }
            } catch (Throwable ignored) {
            } finally {
                mEngine = null;
            }

            mOriginQueue = AudioSourceQueue.empty();
            mCurrentQueue = AudioSourceQueue.empty();

            mABEngine.reset();

            mIsPlayingFlag = false;
            mIsPreparedFlag = false;

            notifyPlaybackCompleted();
        }
    }

    /**
     * Creates an new instance of {@link MediaPlayer}.
     * {@link PlayerImpl#mOnErrorListener} is used as the error handler and
     * {@link PlayerImpl#mOnCompletionListener} is used as the completion handler.
     * @return an new instance of {@link MediaPlayer}.
     */
    @NonNull
    private MediaPlayer createEngine() {
        final MediaPlayer engine = new MediaPlayer();

        if (mUseWakeLocks) {
            // WakeLock will help us prevent playback interruptions.
            // See https://stackoverflow.com/a/53093684/9437681
            engine.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        }

        engine.setAuxEffectSendLevel(1.0f);
        engine.setAudioStreamType(AudioManager.STREAM_MUSIC);

        final AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build();
        engine.setAudioAttributes(attrs);

        engine.setOnErrorListener(mOnErrorListener);
        engine.setOnCompletionListener(mOnCompletionListener);

        return engine;
    }

    private Runnable _handleEngineError(final MediaPlayer mp, final int what, final int extra) {
        return new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Handle engine error: what=" + what + ", extra=" + extra);

                synchronized (mEngineLock) {

                    final MediaPlayer currentEngine = mEngine;
                    if (currentEngine != mp) {
                        // This is not our engine.
                        // We don't deal with this.
                        return;
                    }

                    // Always reporting errors to better understand what problems users do experience
                    report(new PlayerException(MediaPlayerErrors.getErrorMessage(what, extra)));

                    // If it's a critical error, then the engine is not valid anymore.
                    // In this case, the current engine instance should be released and gc-ed.
                    if (isCriticalEngineError(what, extra)) {

                        mPlayerJournal.logMessage("[!] Critical engine error");

                        // Resetting internal flags
                        mIsPreparedFlag = false;
                        mIsPreparedFlag = false;

                        try {
                            // Trying to release the instance
                            if (currentEngine != null) currentEngine.release();
                        } catch (Throwable error) {
                            report(error);
                            mPlayerJournal.logError("Failed to release the broken engine", error);
                        }

                        // Let it be gc-ed
                        mEngine = null;

                        // Fine. The old engine instance has been released.
                        // Now we need to check if there is an audio source set as the current item.
                        // If so, we will create a new engine instance and prepare it for this audio source.

                        final AudioSource currentItem = mCurrentItem;

                        if (currentItem != null) {

                            // OK, there is an audio source. Gotta prepare a new engine for it.
                            mPlayerJournal.logMessage("Create new engine");

                            final MediaPlayer newEngine = createEngine();

                            mEngine = newEngine;

                            // TODO: try also to restore the playback position
                            try {
                                newEngine.reset();
                                newEngine.setDataSource(currentItem.getSource());

                                try {
                                    // Setting playback params. Must be done before engine preparation,
                                    // so that the engine does not start playing automatically.
                                    PlaybackParams playbackParams = newEngine.getPlaybackParams();
                                    playbackParams.setSpeed(mPlaybackSpeed);
                                    playbackParams.setPitch(mPlaybackPitch);
                                    newEngine.setPlaybackParams(playbackParams);
                                } catch (IllegalStateException error) {
                                    report(error);
                                    mPlayerJournal.logMessage("Failed to set playback params for the new engine");
                                }

                                newEngine.prepare();

                                mIsPreparedFlag = true;
                                mMediaPlayerHook.attachAudioEffects(newEngine);
                                mObserverRegistry.dispatchPrepared(newEngine.getDuration(), 0);
                            } catch (Throwable error) {
                                report(error);
                                mPlayerJournal.logMessage("Failed to set up the new engine");
                            }

                        }

                        mObserverRegistry.dispatchPlaybackPaused();

                    }
                }

            }
        };
    }

    @NonNull
    private Runnable _reset() {
        return new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Reset");

                // We reset everything here

                mABEngine.reset();

                synchronized (mEngineLock) {

                    mIsPreparedFlag = false;
                    mIsPlayingFlag = false;

                    final MediaPlayer engine = mEngine;
                    // Trying to reset the engine, if it's not null
                    if (engine != null) {
                        try {
                            engine.reset();
                        } catch (Throwable error) {
                            report(error);
                            mPlayerJournal.logMessage("Failed to reset the current engine");
                        }
                    }
                }

                mCurrentItem = null;
                mCurrentPositionInQueue = NO_POSITION_IN_QUEUE;

                mObserverRegistry.dispatchAudioSourceChanged(null, NO_POSITION_IN_QUEUE);
                mObserverRegistry.dispatchPlaybackPaused();

            }
        };
    }

    @NonNull
    private Runnable _skipToNext(final boolean byUser) {
        return new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Skip to next");

                final AudioSourceQueue queue = mCurrentQueue;
                AudioSource currentItem = mCurrentItem;
                final @RepeatMode int repeatMode = mRepeatMode;
                final @ShuffleMode int shuffleMode = mShuffleMode;
                int currentPositionInQueue = mCurrentPositionInQueue;
                boolean startPlaying = mIsPlayingFlag;

                if (queue.isEmpty()) {
                    // How is it possible?
                    _reset().run();
                    return;
                }

                if (!byUser && repeatMode == Player.REPEAT_ONE) {
                    // everything remains the same
                } else {
                    // the next position is the current one + 1
                    currentPositionInQueue++;

                    if (currentPositionInQueue >= queue.getLength()) {
                        // We've reached the end of the queue => so we get to the start of the queue
                        currentPositionInQueue = 0;
                        currentItem = queue.getItemAt(0);
                        // Since we've reached the end of the queue, we need to check ifr we need to start playing
                        startPlaying = (mIsPlayingFlag && byUser)
                                || (mIsPlayingFlag && repeatMode == Player.REPEAT_PLAYLIST);
                    } else {
                        if (currentPositionInQueue >= 0) {
                            // The position is valid
                            currentItem = queue.getItemAt(currentPositionInQueue);
                        } else {
                            // Undefined state, in fact
                            currentItem = null;
                        }
                    }
                }

                mCurrentItem = currentItem;
                mCurrentPositionInQueue = currentPositionInQueue;
                _handleSource(currentItem, 0, startPlaying).run();

            }
        };
    }

    @NonNull
    private Runnable _handleSource(@Nullable final AudioSource item, final int playbackPosition, final boolean startPlaying) {
        return new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Handle source: " + info(item) + ", seek_pos=" + playbackPosition + ", play=" + startPlaying);

                // The current audio source is changed => gotta reset A-B
                mABEngine.reset();

                // Reset the speed if it should not be persisted
                if (!mIsPlaybackSpeedPersisted) {
                    mPlaybackSpeed = SPEED_NORMAL;
                }
                // Reset the pitch if it should not be persisted
                if (!mIsPlaybackPitchPersisted) {
                    mPlaybackPitch = PITCH_NORMAL;
                }

                mObserverRegistry.dispatchAudioSourceChanged(item, mCurrentPositionInQueue);

                if (item == null) {

                    // The source is null, so we need to reset the engine (of course, if the engine is not null)

                    synchronized (mEngineLock) {

                        mIsPreparedFlag = false;
                        mIsPlayingFlag = false;

                        final MediaPlayer engine = mEngine;
                        if (engine != null) {
                            try {
                                engine.reset();
                            } catch (Throwable error) {
                                report(error);
                                mPlayerJournal.logMessage("Failed to reset the current engine");
                            }
                        }
                    }

                    // It's better to dispatch that the playback is paused,
                    // because we don't know about its state before calling this method.
                    mObserverRegistry.dispatchPlaybackPaused();
                    return;
                }

                synchronized (mEngineLock) {

                    mIsPreparedFlag = false;
                    mIsPlayingFlag = startPlaying;

                    MediaPlayer engine = mEngine;
                    if (engine == null) {
                        // Creating a new engine, if the current is null
                        mEngine = engine = createEngine();
                    }

                    try {
                        engine.reset();
                        try {
                            // First, try to set the data source by the filepath
                            String filepath = item.getSource();
                            mPlayerJournal.logMessage("Set data source from path: " + filepath);
                            engine.setDataSource(filepath);
                        } catch (Throwable error) {
                            // If failed, try to set the data source by the uri
                            if (item instanceof MediaStoreRow) {
                                Uri uri = ((MediaStoreRow) item).getUri();
                                mPlayerJournal.logMessage("Set data source from uri: " + uri.toString());
                                engine.setDataSource(mContext, uri);
                            } else {
                                throw error;
                            }
                        }

                        try {
                            // Setting playback params. Must be done before engine preparation,
                            // so that the engine does not start playing automatically.
                            PlaybackParams playbackParams = engine.getPlaybackParams();
                            playbackParams.setSpeed(mPlaybackSpeed);
                            playbackParams.setPitch(mPlaybackPitch);
                            engine.setPlaybackParams(playbackParams);
                        } catch (IllegalStateException error) {
                            report(error);
                            mPlayerJournal.logError("Failed to set playback params for the current engine", error);
                        }

                        engine.prepare();

                        mIsPreparedFlag = true;

                        mMediaPlayerHook.attachAudioEffects(engine);

                        if (playbackPosition > 0) {
                            engine.seekTo(playbackPosition);
                        }

                        // Dispatch that it's prepared after the audio fx is applied and the progress is sought
                        mObserverRegistry.dispatchPrepared(engine.getDuration(), engine.getCurrentPosition());

                        if (startPlaying && tryRequestAudioFocus()) {
                            mIsPlayingFlag = true;
                            engine.start();
                            maybeAdjustVolume();
                            mObserverRegistry.dispatchPlaybackStarted();
                        } else {
                            mIsPlayingFlag = false;
                            mObserverRegistry.dispatchPlaybackPaused();
                        }
                    } catch (Throwable error) {
                        report(error);
                        mPlayerJournal.logError("Failed to set up the current engine", error);
                        mIsPlayingFlag = false;
                        mObserverRegistry.dispatchPlaybackPaused();
                    }
                }

            }
        };
    }

    @Override
    public void registerObserver(@NonNull PlayerObserver observer) {
        if (isShutdown()) return;
        mObserverRegistry.register(observer);
    }

    @Override
    public void unregisterObserver(@NonNull PlayerObserver observer) {
        if (isShutdown()) return;
        mObserverRegistry.unregister(observer);
    }

    private Runnable _prepareByPosition(@NonNull AudioSourceQueue queue, int positionInQueue, boolean startPlaying, int playbackPosition) {
        return new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Prepare by position: " + info(queue)
                        + ", pos_in_queue=" + positionInQueue
                        + ", play=" + startPlaying
                        + ", seek_pos=" + playbackPosition);

                // First, saving the original queue
                mOriginQueue = queue;

                // Defining the target item
                @Nullable
                final AudioSource targetItem;
                if (positionInQueue >= 0 && positionInQueue < queue.getLength()) {
                    targetItem = queue.getItemAt(positionInQueue);
                } else if (!queue.isEmpty()) {
                    targetItem = queue.getItemAt(positionInQueue);
                } else {
                    targetItem = null;
                }

                // The current queue will be cloned from the original
                final AudioSourceQueue currentQueue = queue.createCopy();

                // Then, we need to configure the new queue according to the current shuffle mode
                // and define the position of the target item in the result queue
                final int targetPosition;
                if (mShuffleMode == Player.SHUFFLE_ON) {
                    if (targetItem != null) {
                        currentQueue.shuffleWithItemInFront(targetItem);
                    } else {
                        currentQueue.shuffle();
                    }
                    targetPosition = currentQueue.indexOf(targetItem);
                } else {
                    targetPosition = positionInQueue;
                }
                mCurrentQueue = currentQueue;

                mObserverRegistry.dispatchQueueChanged(currentQueue);

                mCurrentPositionInQueue = targetPosition;
                mCurrentItem = targetItem;

                _handleSource(targetItem, playbackPosition, startPlaying).run();
            }
        };
    }

    @Override
    public void prepareByTarget(
            @NonNull final AudioSourceQueue queue,
            @NonNull final AudioSource target,
            final boolean startPlaying,
            final int playbackPosition) {

        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                final int positionInQueue = queue.indexOf(target);
                _prepareByPosition(queue, positionInQueue, startPlaying, playbackPosition).run();
            }
        };

        processEngineTask(task, null, true);
    }

    @Override
    public void prepareByPosition(
            @NonNull AudioSourceQueue queue,
            int positionInQueue,
            boolean startPlaying,
            int playbackPosition) {

        if (isShutdown()) return;

        final Runnable task = _prepareByPosition(queue, positionInQueue, startPlaying, playbackPosition);
        processEngineTask(task, null, true);
    }

    @Override
    public void skipToPrevious() {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Skip to previous");

                final boolean byUser = true;

                final AudioSourceQueue queue = mCurrentQueue;
                final @RepeatMode int repeatMode = mRepeatMode;
                int positionInQueue = mCurrentPositionInQueue;
                AudioSource item = mCurrentItem;

                if (queue.isEmpty()) {
                    // How is it possible?
                    return;
                }

                if (!byUser && repeatMode == Player.REPEAT_ONE) {
                    // everything remains the same
                } else {
                    // The previous position is the current one - 1
                    positionInQueue--;

                    if (positionInQueue < 0) {
                        // We've reached the start of the queue => so we get to the ned of the queue
                        positionInQueue = queue.getLength() - 1;;
                    }

                    if (positionInQueue >= 0) {
                        // The position is valid
                        item = queue.getItemAt(positionInQueue);
                    } else {
                        // Undefined state, in fact
                        item = null;
                    }
                }

                mCurrentPositionInQueue = positionInQueue;
                mCurrentItem = item;

                _handleSource(item, 0, mIsPlayingFlag).run();

            }
        };

        // Associating the task with TOKEN_SKIP_TO, but not cancelling the pending ones,
        // because it's OK to process multiple skipToPrevious and skipToNext tasks in a row.
        processEngineTask(task, TOKEN_SKIP_TO, false);
    }

    @Override
    public void skipToNext() {
        if (isShutdown()) return;

        final Runnable task = _skipToNext(true);
        // Associating the task with TOKEN_SKIP_TO, but not cancelling the pending ones,
        // because it's OK to process multiple skipToPrevious and skipToNext tasks in a row.
        processEngineTask(task, TOKEN_SKIP_TO, false);
    }

    @Override
    public void skipTo(final int position, final boolean forceStartPlaying) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Skip to: pos_in_queue=" + position + ", force_play=" + forceStartPlaying);

                final AudioSourceQueue queue = mCurrentQueue;

                final int currentPositionInQueue = mCurrentPositionInQueue;

                if (position == currentPositionInQueue) {
                    // Good, we're at this position already
                    if (forceStartPlaying) {
                        _start().run();
                    }
                    return;
                }

                if (position < 0 || position >= queue.getLength()) {
                    // No sir, it won't work
                    return;
                }

                final AudioSource item = queue.getItemAt(position);

                mCurrentPositionInQueue = position;
                mCurrentItem = item;

                _handleSource(item, 0, mIsPlayingFlag || forceStartPlaying).run();

            }
        };

        // Associating the task with TOKEN_SKIP_TO and cancelling the pending ones.
        processEngineTask(task, TOKEN_SKIP_TO, true);
    }

    @Override
    public void skipTo(@NonNull final AudioSource item, final boolean forceStartPlaying) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Skip to: " + info(item));

                final AudioSourceQueue queue = mCurrentQueue;

                final int newPositionInQueue = queue.indexOf(item);
                // OK, the queue contains the given item
                if (newPositionInQueue >= 0) {
                    mCurrentPositionInQueue = newPositionInQueue;
                    mCurrentItem = item;
                    _handleSource(item, 0, mIsPlayingFlag || forceStartPlaying).run();
                }

            }
        };

        // Associating the task with TOKEN_SKIP_TO and cancelling the pending ones.
        processEngineTask(task, TOKEN_SKIP_TO, true);
    }

    @Override
    public boolean isPrepared() {
        return mIsPreparedFlag;
    }

    /**
     * Checks if the player is actually playing now.
     * Delegates the call to the engine. If the engine is null, then it's considered not playing.
     * The engine must also be prepared, if not, then it's considered not playing.
     * @return true if the player is actually playing now, false - otherwise.
     */
    public boolean isActuallyPlaying() {
        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return false;

            final MediaPlayer engine = mEngine;
            if (engine == null) return false;

            try {
                return engine.isPlaying();
            } catch (Throwable error) {
                report(error);
                return false;
            }
        }
    }

    @Override
    public boolean isPlaying() {
        return mIsPlayingFlag;
    }

    @Override
    public int getAudiSessionId() {
        if (isShutdown()) return NO_AUDIO_SESSION;

        synchronized (mEngineLock) {
            final MediaPlayer engine = mEngine;
            if (engine == null) return NO_AUDIO_SESSION;

            try {
                return engine.getAudioSessionId();
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to get audio session ID", error);
                return NO_AUDIO_SESSION;
            }
        }
    }

    @Nullable
    @Override
    public AudioSource getCurrent() {
        return mCurrentItem;
    }

    @Override
    public int getCurrentPositionInQueue() {
        return mCurrentPositionInQueue;
    }

    @Nullable
    @Override
    public AudioSourceQueue getCurrentQueue() {
        return mCurrentQueue;
    }

    @Override
    public int getProgress() {
        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return 0;

            final MediaPlayer engine = mEngine;
            if (engine == null) return 0;

            try {
                return engine.getCurrentPosition();
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to get progress", error);
                return 0;
            }
        }
    }

    @Override
    public void seekTo(int position) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {
                synchronized (mEngineLock) {

                    if (!mIsPreparedFlag) return;

                    final MediaPlayer engine = mEngine;
                    if (engine == null) return;

                    mPlayerJournal.logMessage("Seek to: " + position);

                    try {
                        engine.seekTo(position);
                        maybeAdjustVolume();
                        mObserverRegistry.dispatchSoughtTo(position);
                    } catch (Throwable error) {
                        report(error);
                        mPlayerJournal.logError("Failed to seek", error);
                    }
                }
            }
        };

        processEngineTask(task);
    }

    @Override
    public int getDuration() {
        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return 0;

            final MediaPlayer engine = mEngine;
            if (engine == null) return 0;

            try {
                return engine.getDuration();
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to get duration", error);
                return 0;
            }
        }
    }

    @NonNull
    private Runnable _start() {
        return new Runnable() {
            @Override
            public void run() {

                synchronized (mEngineLock) {

                    if (!mIsPreparedFlag) return;

                    final MediaPlayer engine = mEngine;
                    if (engine == null) return;

                    mPlayerJournal.logMessage("Start");

                    // The focus should be granted as well
                    if (tryRequestAudioFocus()) {

                        mIsPlayingFlag = true;

                        try {
                            engine.start();
                            maybeAdjustVolume();
                            mObserverRegistry.dispatchPlaybackStarted();
                        } catch (Throwable error) {
                            report(error);
                            mPlayerJournal.logError("Failed to start", error);
                        }

                    }
                }

            }
        };
    }

    @Override
    public void start() {
        processEngineTask(_start());
    }

    @Override
    public void pause() {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                synchronized (mEngineLock) {

                    if (!mIsPreparedFlag) return;

                    final MediaPlayer engine = mEngine;
                    if (engine == null) return;

                    // Here, we check if the pause can be skipped.
                    // This is to avoid unnecessary pauses,
                    // because the media player engine may break
                    // if it actually wasn't playing until now.
                    boolean canSkipThePause = false;
                    try {
                        canSkipThePause = !mIsPlayingFlag && !engine.isPlaying();
                    } catch (Throwable error) {
                        mPlayerJournal.logError("Failed to check if the pause can be skipped", error);
                    }
                    if (canSkipThePause) return;

                    mIsPlayingFlag = false;

                    mPlayerJournal.logMessage("Pause");

                    try {
                        engine.pause();
                        mObserverRegistry.dispatchPlaybackPaused();
                    } catch (Throwable error) {
                        report(error);
                        mPlayerJournal.logError("Failed to pause", error);
                    }
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public void toggle() {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                synchronized (mEngineLock) {

                    if (!mIsPreparedFlag) return;

                    final MediaPlayer engine = mEngine;
                    if (engine == null) return;

                    final boolean oldIsPlayingFlag = mIsPlayingFlag;
                    final boolean newIsPlayingFlag = !oldIsPlayingFlag;

                    mPlayerJournal.logMessage("Toggle");

                    try {
                        if (newIsPlayingFlag) {

                            // Only if the focus is granted
                            if (tryRequestAudioFocus()) {
                                mIsPlayingFlag = true;
                                engine.start();
                                maybeAdjustVolume();
                                mObserverRegistry.dispatchPlaybackStarted();
                            }

                        } else {
                            mIsPlayingFlag = false;
                            engine.pause();
                            mObserverRegistry.dispatchPlaybackPaused();
                        }

                    } catch (Throwable error) {
                        report(error);
                        mPlayerJournal.logError("Failed to toggle", error);
                    }
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public void update(@NonNull final AudioSource item) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Update audio source: " + info(item));

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;
                final AudioSource currentItem = mCurrentItem;

                if (originQueue != null) {
                    originQueue.replaceAllWithSameId(item);
                }

                currentQueue.replaceAllWithSameId(item);

                if (currentItem != null && AudioSources.areSourcesTheSame(currentItem, item)) {
                    mCurrentItem = item;
                    mObserverRegistry.dispatchAudioSourceUpdated(item);
                }

            }
        };

        processEngineTask(task);
    }

    private Runnable _resolveUndefinedState(boolean startPlaying) {
        return new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Resolve undefined state");

                final AudioSourceQueue currentQueue = mCurrentQueue;
                int currentPositionInQueue = mCurrentPositionInQueue;
                AudioSource currentItem = mCurrentItem;

                if (currentQueue.isEmpty()) {
                    mCurrentPositionInQueue = NO_POSITION_IN_QUEUE;
                    mCurrentItem = null;
                    _reset().run();
                } else {
                    if (currentPositionInQueue < 0) {
                        currentPositionInQueue = 0;
                    } else if (currentPositionInQueue >= currentQueue.getLength()) {
                        currentPositionInQueue = currentQueue.getLength() - 1;
                    }

                    currentItem = currentQueue.getItemAt(currentPositionInQueue);

                    mCurrentItem = currentItem;
                    mCurrentPositionInQueue = currentPositionInQueue;

                    _handleSource(currentItem,0, startPlaying).run();
                }

            }
        };
    }

    @Override
    public void removeAt(final int position) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Remove item at: pos_in_queue" + position);

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;
                final AudioSource targetItem = currentQueue.getItemAt(position);
                int currentPositionInQueue = mCurrentPositionInQueue;

                if (originQueue != null) {
                    originQueue.remove(targetItem);
                }
                currentQueue.removeAt(position);

                if (position < currentPositionInQueue) {
                    currentPositionInQueue--;
                    mCurrentPositionInQueue = currentPositionInQueue;
                    mObserverRegistry.dispatchPositionInQueueChanged(currentPositionInQueue);
                } else if (position == currentPositionInQueue) {
                    _resolveUndefinedState(mIsPlayingFlag).run();
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public void removeAll(@NonNull final Collection<? extends AudioSource> items) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Remove items: count=" + items.size());

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;
                int currentPositionInQueue = mCurrentPositionInQueue;

                for (AudioSource item : items) {

                    int position = -1;
                    while ((position = mCurrentQueue.indexOf(item)) != -1) {
                        if (position <= currentPositionInQueue) {
                            currentPositionInQueue--;
                        }

                        currentQueue.removeAt(position);
                    }

                }

                if (originQueue != null) {
                    originQueue.removeAll(items);
                }

                if (currentQueue.isEmpty() || items.contains(mCurrentItem)) {
                    _resolveUndefinedState(mIsPlayingFlag).run();
                } else {
                    mCurrentPositionInQueue = currentPositionInQueue;
                    mObserverRegistry.dispatchPositionInQueueChanged(currentPositionInQueue);
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public void add(@NonNull AudioSource item) {
        addAll(Collections.singletonList(item));
    }

    @Override
    public void addAll(@NonNull final List<? extends AudioSource> items) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Add items: count=" + items.size());

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;

                if (originQueue != null) {
                    originQueue.addAll(items);
                }
                currentQueue.addAll(items);

                if (mCurrentItem == null && !currentQueue.isEmpty()) {
                    final int currentPositionInQueue = 0;
                    final AudioSource currentItem = currentQueue.getItemAt(currentPositionInQueue);

                    mCurrentItem = currentItem;
                    mCurrentPositionInQueue = currentPositionInQueue;

                    _handleSource(currentItem, 0, false).run();
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public void addNext(@NonNull AudioSource item) {
        addAllNext(Collections.singletonList(item));
    }

    @Override
    public void addAllNext(@NonNull final List<? extends AudioSource> items) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Add item next: count=" + items.size());

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;
                int currentPositionInQueue = mCurrentPositionInQueue;
                AudioSource currentItem = mCurrentItem;

                final int targetPosition = Math.max(0, currentPositionInQueue);

                if (originQueue != null) {
                    originQueue.addAll(targetPosition + 1, items);
                }
                currentQueue.addAll(targetPosition + 1, items);

                if (currentItem == null && !currentQueue.isEmpty()) {
                    currentPositionInQueue = 0;
                    currentItem = currentQueue.getItemAt(currentPositionInQueue);

                    mCurrentPositionInQueue = currentPositionInQueue;
                    mCurrentItem = currentItem;

                    _handleSource(currentItem, 0, false).run();
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public void moveItem(final int fromPosition, final int toPosition) {
        if (isShutdown()) return;

        if (fromPosition == toPosition) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Move item: from=" + fromPosition + ", to=" + toPosition);

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;
                int currentPositionInQueue = mCurrentPositionInQueue;
                //AudioSource currentItem = mCurrentItem;

                if (originQueue != null) {
                    if (fromPosition >= 0 && toPosition >= 0) {
                        originQueue.moveItem(fromPosition, toPosition);
                    }
                }

                // Finding out what the current position will be
                if (currentPositionInQueue == fromPosition) {
                    currentPositionInQueue = toPosition;
                } else if (currentPositionInQueue < fromPosition) {
                    if (currentPositionInQueue < toPosition) {
                        // no changes
                    } else {
                        currentPositionInQueue++;
                    }
                } else if (currentPositionInQueue > fromPosition) {
                    if (currentPositionInQueue > toPosition) {
                        // no changes
                    } else {
                        currentPositionInQueue--;
                    }
                }

                if (fromPosition >= 0 && toPosition >= 0) {
                    currentQueue.moveItem(fromPosition, toPosition);
                }

                if (mCurrentPositionInQueue != currentPositionInQueue) {
                    mCurrentPositionInQueue = currentPositionInQueue;
                    mObserverRegistry.dispatchPositionInQueueChanged(currentPositionInQueue);
                }

            }
        };

        processEngineTask(task);
    }

    @Override
    public boolean isAPointed() {
        return mABEngine.isAPointed();
    }

    @Override
    public boolean isBPointed() {
        return mABEngine.isBPointed();
    }

    @Override
    public void pointA(int position) {
        mABEngine.pointA(position);
    }

    @Override
    public void pointB(int position) {
        mABEngine.pointB(position);
    }

    @Override
    public void resetAB() {
        mABEngine.reset();
    }

    private void tryRewind(int intervalValue) {
        if (isShutdown()) return;

        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return;

            final MediaPlayer engine = mEngine;
            if (engine == null) return;

            mPlayerJournal.logMessage("Rewind: interval=" + intervalValue);

            try {
                final int newPosition = engine.getCurrentPosition() + intervalValue;
                engine.seekTo(newPosition);
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to rewind", error);
            }

        }
    }

    @Override
    public void rewindForward(int interval) {
        tryRewind(interval);
    }

    @Override
    public void rewindBackward(int interval) {
        tryRewind(-interval);
    }

    public void setVolume(float level) {
        synchronized (mEngineLock) {
            final MediaPlayer engine = mEngine;
            if (engine == null) return;

            mPlayerJournal.logMessage("Set volume");

            try {
                engine.setVolume(level, level);
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to set volume", error);
            }
        }
    }

    /**
     * Starts {@link Timer}, which will adjust the volume for smooth playback fading.
     * This method should be called once during the player's life.
     * It is better to make the method call lazy.
     * TODO: consider making the call lazy instead of doing the call in the constructor
     */
    private void startPlaybackFadingTimer() {
        if (isShutdown()) return;

        final Timer oldTimer = mPlaybackFadingTimer;
        if (oldTimer != null) {
            oldTimer.purge();
            oldTimer.cancel();
        }

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                while (!isShutdown()) {

                    maybeAdjustVolume();

                    try {
                        // Sleeping until the next volume adjustment
                        Thread.sleep(VOLUME_ADJUSTMENT_INTERVAL);
                    } catch (InterruptedException error) {
                        return;
                    }
                }
            }
        };

        final Timer newTimer = new Timer("PlaybackFadingTimer");

        newTimer.schedule(task, 0);

        mPlaybackFadingTimer = newTimer;
    }

    /**
     * Adjusts the volume level according to the current {@link PlaybackFadingStrategy}.
     * The method should be called constantly throughout the player's life.
     * There is also a need to call this method every time the engine is started.
     */
    private void maybeAdjustVolume() {
        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return;

            final MediaPlayer engine = mEngine;
            if (engine == null) return;

            try {

                // Do NOT log here, otherwise the journal will overflow

                final PlaybackFadingStrategy strategy = mPlaybackFadingStrategy;

                final float level;
                if (strategy == null) {
                    // No strategy - no fading
                    level = PlaybackFadingStrategy.NORMAL_LEVEL;
                } else {
                    final int progress = engine.getCurrentPosition();
                    final int duration = engine.getDuration();
                    // This is where the strategy works
                    level = strategy.calculateLevel(progress, duration);
                }

                final float volume = VolumeHelper.computeVolume(level);

                engine.setVolume(volume, volume);
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to adjust volume", error);
            }
        }

    }

    @Nullable
    @Override
    public PlaybackFadingStrategy getPlaybackFadingStrategy() {
        return mPlaybackFadingStrategy;
    }

    @Override
    public void setPlaybackFadingStrategy(@Nullable PlaybackFadingStrategy strategy) {
        mPlayerJournal.logMessage("Set playback fading strategy");

        mPlaybackFadingStrategy = strategy;

        // It's better to adjust the volume on the engine thread,
        // as this method is synchronized on the engine,
        // so there might be blocking.
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                maybeAdjustVolume();
            }
        };

        processEngineTask(task);
    }

    @Override
    public boolean isSpeedPersisted() {
        return mIsPlaybackSpeedPersisted;
    }

    @Override
    public void setSpeedPersisted(boolean isPersisted) {
        mIsPlaybackSpeedPersisted = isPersisted;
    }

    @Override
    public float getSpeed() {
        if (isShutdown()) return SPEED_NORMAL;
        return mPlaybackSpeed;
    }

    @Override
    public void setSpeed(float speed) {
        if (isShutdown()) return;

        // Validating the speed
        speed = MathUtil.clamp(speed, SPEED_RANGE);

        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return;

            final MediaPlayer engine = mEngine;
            if (engine == null) return;

            mPlayerJournal.logMessage("Set speed: " + speed);

            try {
                boolean wasPlaying = mIsPreparedFlag && engine.isPlaying();
                final PlaybackParams params = engine.getPlaybackParams();
                params.setSpeed(speed);
                engine.setPlaybackParams(params);
                if (!wasPlaying) {
                    boolean isActuallyPlaying = engine.isPlaying();
                    // As per docs, setting playback params with a non-zero speed
                    // causes MediaPlayer to start playing. This is not what we want,
                    // so we call the 'pause' method here.
                    if (isActuallyPlaying) {
                        engine.pause();
                    }
                }
                mPlaybackSpeed = speed;
                mObserverRegistry.dispatchSpeedChanged(speed);
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to set speed", error);
            }
        }
    }

    @Override
    public boolean isPitchPersisted() {
        return mIsPlaybackPitchPersisted;
    }

    @Override
    public void setPitchPersisted(boolean isPersisted) {
        mIsPlaybackPitchPersisted = isPersisted;
    }

    @Override
    public float getPitch() {
        if (isShutdown()) return PITCH_NORMAL;
        return mPlaybackPitch;
    }

    @Override
    public void setPitch(float pitch) {
        if (isShutdown()) return;

        // Validating the pitch
        pitch = MathUtil.clamp(pitch, PITCH_RANGE);

        synchronized (mEngineLock) {

            if (!mIsPreparedFlag) return;

            final MediaPlayer engine = mEngine;
            if (engine == null) return;

            mPlayerJournal.logMessage("Set pitch");

            try {
                final PlaybackParams params = engine.getPlaybackParams();
                params.setPitch(pitch);
                engine.setPlaybackParams(params);
                mPlaybackPitch = pitch;
                mObserverRegistry.dispatchPitchChanged(pitch);
            } catch (Throwable error) {
                report(error);
                mPlayerJournal.logError("Failed to set pitch", error);
            }
        }
    }

    @Override
    public int getShuffleMode() {
        return mShuffleMode;
    }

    @Override
    public void setShuffleMode(final int mode) {
        if (isShutdown()) return;

        final Runnable task = new Runnable() {
            @Override
            public void run() {

                mPlayerJournal.logMessage("Set shuffle mode: " + mode);

                mShuffleMode = mode;

                final AudioSourceQueue originQueue = mOriginQueue;
                final AudioSourceQueue currentQueue = mCurrentQueue;
                final AudioSource currentItem = mCurrentItem;

                if (mode == Player.SHUFFLE_OFF) {
                    final AudioSourceQueue targetQueue =
                            originQueue != null ? originQueue : AudioSourceQueue.empty();

                    currentQueue.copyItemsFrom(targetQueue);
                } else {
                    currentQueue.shuffleWithItemInFront(currentItem);
                }

                final int position = currentQueue.indexOf(currentItem);
                mCurrentPositionInQueue = position;
                mObserverRegistry.dispatchPositionInQueueChanged(position);

                mObserverRegistry.dispatchShuffleModeChanged(mode);

            }
        };

        processEngineTask(task);
    }

    @Override
    public int getRepeatMode() {
        return mRepeatMode;
    }

    @Override
    public void setRepeatMode(int mode) {
        if (isShutdown()) return;

        mPlayerJournal.logMessage("Set repeat mode: " + mode);

        mRepeatMode = mode;
        mObserverRegistry.dispatchRepeatModeChanged(mode);
    }

    @Override
    public void shutdown() {
        if (mShutdown.getAndSet(true)) return;

        mPlayerJournal.logMessage("Shutdown");

        // Cancelling pending engine tasks
        cancelAllEngineTasks();

        // Resetting the A-B engine
        mABEngine.reset();

        // Resetting the playback fading timer
        final Timer oldTimer = mPlaybackFadingTimer;
        if (oldTimer != null) {
            oldTimer.purge();
            oldTimer.cancel();
        }
        mPlaybackFadingTimer = null;

        // Releasing the hook
        mMediaPlayerHook.releaseAudioEffects();

        // Resetting the engine and internal flags
        synchronized (mEngineLock) {

            mIsPreparedFlag = false;
            mIsPlayingFlag = false;

            final MediaPlayer engine = mEngine;
            if (engine != null) {
                try {
                    engine.release();
                } catch (Throwable error) {
                    report(error);
                }
            }
            mEngine = null;
        }

        // Resetting internal state
        mOriginQueue = null;
        mCurrentItem = null;
        mCurrentPositionInQueue = NO_POSITION_IN_QUEUE;

        // Finally, notifying about the shutdown and unregistering all observers
        mObserverRegistry.dispatchShutdown(true);
    }

    /**
     * Thread-safe implementation of A-B engine.
     * The player can simply delegate its AB-related methods to this engine.
     */
    private final class ABEngine {

        private final static int MIN_AB_INTERVAL = 250;

        @Nullable
        volatile Integer mAPoint;
        @Nullable
        volatile Integer mBPoint;

        @Nullable
        volatile Timer mCurrentTimer;

        synchronized boolean isAPointed() {
            return mAPoint != null;
        }

        synchronized boolean isBPointed() {
            return mBPoint != null;
        }

        synchronized void pointA(int position) {
            mPlayerJournal.logMessage("Point A: pos=" + position);

            final int duration = getDuration();

            int validAPoint = position;
            if (validAPoint > duration - MIN_AB_INTERVAL) {
                validAPoint = duration - MIN_AB_INTERVAL;
            }

            final Integer bPoint = mBPoint;
            if (bPoint != null && position > bPoint - MIN_AB_INTERVAL) {
                validAPoint = bPoint - MIN_AB_INTERVAL;
            }

            if (validAPoint < 0) {
                validAPoint = 0;
            }

            this.mAPoint = validAPoint;
            _start();
            mObserverRegistry.dispatchABChanged(true, bPoint != null);
        }

        synchronized void pointB(int position) {
            mPlayerJournal.logMessage("Point B: pos=" + position);

            int validBPoint = position;
            if (validBPoint < 0) {
                validBPoint = 0;
            }

            final Integer aPoint = mAPoint;
            if (aPoint != null && position < aPoint + MIN_AB_INTERVAL) {
                validBPoint = aPoint + MIN_AB_INTERVAL;
            }

            final int duration = getDuration();
            if (validBPoint > duration) {
                validBPoint = duration;
            }

            this.mBPoint = validBPoint;
            _start();
            mObserverRegistry.dispatchABChanged(aPoint != null, true);
        }

        synchronized void reset() {
            mPlayerJournal.logMessage("Reset A-B");

            mAPoint = null;
            mBPoint = null;

            final Timer old = mCurrentTimer;
            if (old != null) {
                old.purge();
                old.cancel();
            }
            mCurrentTimer = null;

            mObserverRegistry.dispatchABChanged(false, false);
        }

        private synchronized void _start() {

            // TODO: make sure only one timer is running

            // Canceling the previous timer, if any.
            // Cause we don't want two timers doing the A-B job.
            final Timer old = mCurrentTimer;
            if (old != null) {
                old.purge();
                old.cancel();
            }

            final TimerTask task = new TimerTask() {
                @Override
                public void run() {

                    Integer a;
                    Integer b;

                    synchronized (ABEngine.this) {
                        a = mAPoint;
                        b = mBPoint;
                    }

                    // We're going to do this while the A-B is enabled.
                    // The A-B is considered enabled if and only if both A and B points are not null.
                    while (a != null && b != null) {
                        try {

                            final int pos = getProgress();
                            if (pos < a - 100) {
                                // Changing the progress
                                seekTo(a);
                                continue;
                            }

                            final int sleep = b - pos;
                            if (sleep > 0) {
                                // Sleeping to wait for the point B to be reached
                                Thread.sleep(sleep);
                            }

                            synchronized (ABEngine.this) {
                                a = mAPoint;
                                b = mBPoint;
                            }

                            // Additional check just before seeking the position.
                            // Just to make sure the A-B has not changed after sleeping the thread.
                            if (a == null || b == null) {
                                break;
                            }

                            // Seeking the position to the point A
                            seekTo(a);

                        } catch (InterruptedException error) {
                            break;
                        } catch (Throwable error) {
                            mPlayerJournal.logMessage("Failed to seek for A-B");
                            throw error;
                        }
                    }
                }
            };

            final Timer newTimer = new Timer();

            newTimer.schedule(task, 0);

            // Saving the timer
            mCurrentTimer = newTimer;
        }
    }

}
