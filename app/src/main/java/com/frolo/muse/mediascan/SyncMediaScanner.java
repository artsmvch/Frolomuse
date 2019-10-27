package com.frolo.muse.mediascan;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.frolo.muse.Trace;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Synchronous media scanner.
 * Can be used only one time.
 * Scanning is started by calling {@link #startScanning(long)}
 * Scanning may be aborted by calling {@link #abortScanning()}
 */
public final class SyncMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private final static String TAG = SyncMediaScanner.class.getSimpleName();
    private final MediaScannerConnection mConnection;
    private final List<String> mFiles;
    private final int mFilesCount;
    private final Object mWaiter = new Object();
    private final AtomicInteger mScannedFilesCount = new AtomicInteger(0);
    private final AtomicBoolean mScanned = new AtomicBoolean(false);

    public SyncMediaScanner(Context context, List<String> files) {
        mFiles = files;
        mFilesCount = files.size();
        mConnection = new MediaScannerConnection(context, this);
    }

    public void startScanning(long connectionTimeout) {
        if (mScanned.getAndSet(true)) {
            throw new IllegalStateException("Scanned already");
        }
        synchronized (mWaiter) {
            try {
                mConnection.connect();
                mWaiter.wait(0);
            } catch (InterruptedException e) {
                Trace.e(e);
            }
        }
    }

    public void abortScanning() {
        synchronized (mWaiter) {
            mConnection.disconnect();
            mWaiter.notify();
        }
    }

    @Override
    public void onMediaScannerConnected() {
        Trace.d(TAG, "Scanner connected");
        for (String filepath : mFiles) {
            Trace.d(TAG, "Scanning: [" + filepath + "]");
            mConnection.scanFile(filepath, null);
        }
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        Trace.d(TAG, "Scanned completed: [" + path + "]");
        if (mScannedFilesCount.incrementAndGet() >= mFilesCount) {
            Trace.d(TAG, "All files scanned. Completing work");
            synchronized (mWaiter) {
                mConnection.disconnect();
                mWaiter.notify();
            }
        }
    }
}