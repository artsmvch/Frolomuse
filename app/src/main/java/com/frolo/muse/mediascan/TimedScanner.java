package com.frolo.muse.mediascan;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.AnyThread;

import com.frolo.muse.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


final class TimedScanner {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String LOG_TAG = TimedScanner.class.getSimpleName();

    static TimedScanner create(
            Context ctx,
            Handler engineHandler,
            Handler callbackHandler,
            List<String> files,
            long timeout,
            ScanCallback callback
    ) {
        return new TimedScanner(ctx, engineHandler, callbackHandler, files, timeout, callback);
    }

    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final AtomicBoolean mDisposed = new AtomicBoolean(false);

    private final AtomicInteger mPathCount = new AtomicInteger(0);

    private final BlockingQueue<String> mPendingPaths;

    private final MediaScannerConnection mConnection;

    private final long mTimeout;

    private final Handler mEngineHandler;
    private final Handler mCallbackHandler;

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
            if (!mDisposed.get()) {
                dispatchProgressChanged();
                execScanNextPath();
            }
        }
    };

    private TimedScanner(Context ctx, Handler engineHandler, Handler callbackHandler, List<String> files, long timeout, ScanCallback callback) {
        mEngineHandler = engineHandler;
        mCallbackHandler = callbackHandler;
        mConnection = new MediaScannerConnection(ctx, mClientProxy);
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
            execScanNextPath();
        }
    };

    @AnyThread
    private void execScanNextPath() {
        mEngineHandler.removeCallbacksAndMessages(mCheckTimeoutToken);

        final String path = mPendingPaths.poll();

        if (path != null) {
            if (DEBUG) Log.d(LOG_TAG, "Scanning " + path + ". " + mPendingPaths.size() + " paths left");

            mConnection.scanFile(path, null);

            mEngineHandler.postAtTime(mCheckTimeoutTask, mCheckTimeoutToken, mTimeout);
        } else {
            disposeInternal(true);
        }
    }

    //endregion

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
        mCallbackHandler.removeCallbacksAndMessages(mDispatchScanStartedToken);
        mCallbackHandler.postAtTime(mDispatchScanStartedTask, mDispatchScanStartedToken, 0);
    }

    private final Object mDispatchProgressChangedToken = new Object();
    private final Runnable mDispatchProgressChangedTask =
            new Runnable() {
                @Override
                public void run() {
                    ScanCallback callback = mCallback.get();
                    if (callback != null) {
                        int progress = mPathCount.get() - mPendingPaths.size();
                        callback.onProgressChanged(progress, mPathCount.get());
                    }
                }
            };

    @AnyThread
    private void dispatchProgressChanged() {
        mCallbackHandler.removeCallbacksAndMessages(mDispatchProgressChangedToken);
        mCallbackHandler.postAtTime(mDispatchProgressChangedTask, mDispatchProgressChangedToken, 0);
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
        mCallbackHandler.removeCallbacksAndMessages(mDispatchScanCompletedToken);
        mCallbackHandler.postAtTime(mDispatchScanCompleted, mDispatchScanCompletedToken, 0);
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
        mCallbackHandler.removeCallbacksAndMessages(mDispatchScanCancelledToken);
        mCallbackHandler.postAtTime(mDispatchScanCancelled, mDispatchScanCancelledToken, 0);
    }

    //endregion

    interface ScanCallback {

        void onScanStarted();

        void onProgressChanged(int progress, int total);

        void onScanCompleted();

        void onScanCancelled();
    }

}
