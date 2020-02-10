package com.frolo.muse.mediascan;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.AnyThread;

import com.frolo.muse.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


final class TimedScanner {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String LOG_TAG = TimedScanner.class.getSimpleName();

    static TimedScanner create(
            Context ctx, Handler handler, List<String> files,
            long timeout, ScanCallback callback) {

        return new TimedScanner(ctx, handler, files, timeout, callback);
    }

    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final AtomicBoolean mDisposed = new AtomicBoolean(false);

    private final int mPathCount;

    private final BlockingQueue<String> mPendingPaths;

    // The path that is currently being scanned
    private final AtomicReference<String> mCurrentPath = new AtomicReference<>();

    private final MediaScannerConnection mConnection;

    private final long mTimeout;

    private final Handler mHandler;

    private final WeakReference<ScanCallback> mCallback;

    private final MediaScannerConnection.MediaScannerConnectionClient mClientProxy =
            new MediaScannerConnection.MediaScannerConnectionClient() {

        @Override
        public void onMediaScannerConnected() {
            if (!mDisposed.get()) {
                if (DEBUG) Log.d(LOG_TAG, "Connected to MediaScanner");
                dispatchScanStarted();
                execScanNextPath();
            }
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            if (path != null && !path.equals(mCurrentPath.get())) {
                // that's not the path we're expecting
                return;
            }

            if (!mDisposed.get()) {
                dispatchProgressChanged();
                execScanNextPath();
            }
        }
    };

    private TimedScanner(Context ctx, Handler handler, List<String> files, long timeout, ScanCallback callback) {
        mHandler = handler;
        mConnection = new MediaScannerConnection(ctx, mClientProxy);
        mPathCount = files.size();
        mPendingPaths = new LinkedBlockingQueue<>(files);
        mTimeout = timeout;
        mCallback = new WeakReference<>(callback);
    }

    private void disposeInternal(boolean completed) {
        if (!mStarted.get()) {
            if (DEBUG) Log.w(LOG_TAG, "Cannot dispose scanner, because it has not been started yet");
            return;
        }

        if (!mDisposed.getAndSet(true)) {
            mConnection.disconnect();
        }

        if (DEBUG) Log.d(LOG_TAG, "Disposing. Completed=" + completed);

        if (completed) {
            dispatchScanCompleted();
        } else {
            dispatchScanCancelled();
        }
    }

    /*package*/ void start() {
        if (!mStarted.getAndSet(true)) {
            mConnection.connect();
            if (DEBUG) Log.d(LOG_TAG, "Connecting to MediaScanner. Total paths count=" + mPendingPaths.size());
        } else {
            if (DEBUG) Log.w(LOG_TAG, "Cannot start scanning, because it has been started already");
        }
    }

    /*package*/ void dispose() {
        disposeInternal(false);
    }

    //region Scan methods

    private final Object mCheckTimeoutToken = new Object();
    private final Runnable mCheckTimeoutTask = new Runnable() {
        @Override
        public void run() {
            String problematicPath = mCurrentPath.getAndSet(null);
            if (DEBUG) Log.w(LOG_TAG, "Timeout for " + problematicPath + ". Keep on scanning");
            execScanNextPath();
        }
    };

    @AnyThread
    private void execScanNextPath() {
        mHandler.removeCallbacksAndMessages(mCheckTimeoutToken);

        if (mDisposed.get() || !mConnection.isConnected()) {
            // it's over
            return;
        }

        final String path = mPendingPaths.poll();

        if (mPendingPaths.isEmpty()) {
            disposeInternal(true);
        } else {
            if (DEBUG) Log.d(LOG_TAG, "Scanning " + path + ". " + mPendingPaths.size() + " paths left");

            mCurrentPath.set(path);

            mConnection.scanFile(path, null);

            exec(mHandler, mCheckTimeoutTask, mCheckTimeoutToken, mTimeout);
        }
    }

    //endregion

    private void exec(Runnable task, Object token) {
        mHandler.removeCallbacksAndMessages(token);
        if (mHandler.getLooper().getThread() == Thread.currentThread()) {
            task.run();
        } else {
            mHandler.postAtTime(task, token, SystemClock.uptimeMillis());
        }
    }

    private void exec(Handler handler, Runnable task, Object token, long delay) {
        handler.removeCallbacksAndMessages(token);
        handler.postAtTime(task, token, SystemClock.uptimeMillis() + delay);
    }

    //region Dispatchers

    private final Object mDispatchScanStartedToken = new Object();
    private final Runnable mDispatchScanStartedTask =
            new Runnable() {
                @Override
                public void run() {
                    ScanCallback callback = mCallback.get();
                    if (callback != null) {
                        callback.onScanStarted();
                    }
                }
            };

    @AnyThread
    private void dispatchScanStarted() {
        exec(mDispatchScanStartedTask, mDispatchScanStartedToken);
    }

    private final Object mDispatchProgressChangedToken = new Object();
    private final Runnable mDispatchProgressChangedTask =
            new Runnable() {
                @Override
                public void run() {
                    ScanCallback callback = mCallback.get();
                    if (callback != null) {
                        int progress = mPathCount - mPendingPaths.size();
                        callback.onProgressChanged(mPathCount, progress);
                    }
                }
            };

    @AnyThread
    private void dispatchProgressChanged() {
        exec(mDispatchProgressChangedTask, mDispatchProgressChangedToken);
    }

    private final Object mDispatchScanCompletedToken = new Object();
    private final Runnable mDispatchScanCompleted =
            new Runnable() {
                @Override
                public void run() {
                    ScanCallback callback = mCallback.get();
                    if (callback != null) {
                        callback.onScanCompleted();
                    }
                }
            };

    @AnyThread
    private void dispatchScanCompleted() {
        exec(mDispatchScanCompleted, mDispatchScanCompletedToken);
    }

    private final Object mDispatchScanCancelledToken = new Object();
    private final Runnable mDispatchScanCancelled =
            new Runnable() {
                @Override
                public void run() {
                    ScanCallback callback = mCallback.get();
                    if (callback != null) {
                        callback.onScanCancelled();
                    }
                }
            };

    @AnyThread
    private void dispatchScanCancelled() {
        exec(mDispatchScanCancelled, mDispatchScanCancelledToken);
    }

    //endregion

    interface ScanCallback {

        void onScanStarted();

        void onProgressChanged(int total, int progress);

        void onScanCompleted();

        void onScanCancelled();
    }

}
