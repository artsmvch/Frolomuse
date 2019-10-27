package com.frolo.muse.mediascan;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.frolo.muse.Trace;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public final class AsyncMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private final static String TAG = AsyncMediaScanner.class.getSimpleName();
    private final MediaScannerConnection mConnection;
    private final List<String> mFiles;
    private final int mFilesCount;
    private final AtomicInteger mScannedFilesCount = new AtomicInteger(0);
    private final AtomicBoolean mScanned = new AtomicBoolean(false);
    // hold listener in weak reference to avoid any memory leak
    private final WeakReference<Listener> mListener;

    public interface Listener {
        void onStarted();
        void onCompleted();
        void onCancelled();
    }

    /**
     * Constructor
     * @param context context to bind the media scanner connection. It must be application context to avoid any memory leak
     * @param files files to scan
     * @param listener listener to handle completion
     */
    public AsyncMediaScanner(Context context, List<String> files, Listener listener) {
        mFiles = files;
        mFilesCount = files.size();
        mConnection = new MediaScannerConnection(context, this);
        mListener = new WeakReference<>(listener);
    }

    /**
     * Starts scanning.
     * Note that it can be called only once.
     * Otherwise an exception will be thrown.
     */
    public void startScanning() {
        if (mScanned.getAndSet(true)) {
            throw new IllegalStateException("Scanned already");
        }
        Listener l = mListener.get();
        if (l != null) {
            l.onStarted();
        }
        if (mFiles.isEmpty()) {
            Trace.d(TAG, "No files to scan. Completing.");
            if (l != null) {
                l.onCompleted();
            }
            return;
        }
        Trace.d(TAG, "Scanner started. Connecting");
        mConnection.connect();
    }

    /**
     * There is an issue with disconnecting from the connection service.
     * It doesn't work at all!
     * Needs to be investigated.
     */
    public void abortScanning() {
        Trace.d(TAG, "Scanner aborted. Disconnecting");
        mConnection.disconnect();
        Listener l = mListener.get();
        if (l != null) {
            l.onCancelled();
        }
    }

    @Override
    public void onMediaScannerConnected() {
        Trace.d(TAG, "Scanner connected. Starting scanning each file");
        for (String filepath : mFiles) {
            mConnection.scanFile(filepath, null);
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Trace.d(TAG, "Scanned: [" + path + "]");
        if (!mConnection.isConnected()) {
            Trace.w(TAG, "It was disconnected already. Why did we get this call?");
        }
        if (mScannedFilesCount.incrementAndGet() >= mFilesCount) {
            Trace.d(TAG, "All files scanned. Disconnecting");
            mConnection.disconnect();
            Listener l = mListener.get();
            if (l != null) {
                l.onCompleted();
            }
        }
    }

}