package com.frolo.muse.mediascan;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.util.SparseArray;
import android.widget.RemoteViews;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MediaScanService extends Service {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String LOG_TAG = MediaScanService.class.getSimpleName();

    // Timeout for one-file-scanning.
    private static final long SCAN_TIMEOUT = 5_000;

    private static final int RC_CANCEL = 3731;

    // Used for broadcasting
    public static final String ACTION_MEDIA_SCANNING_STATUS = "com.frolo.muse.mediascan.ACTION_MEDIA_SCANNING_STATUS";
    private static final String ACTION_SCAN_MEDIA = "com.frolo.muse.mediascan.ACTION_SCAN_MEDIA";
    private static final String ACTION_CANCEL_SCAN_MEDIA = "com.frolo.muse.mediascan.ACTION_CANCEL_SCAN_MEDIA";

    public static final String EXTRA_MEDIA_SCANNING_STARTED = "media_scanning_started";
    public static final String EXTRA_MEDIA_SCANNING_COMPLETED = "media_scanning_completed";
    public static final String EXTRA_MEDIA_SCANNING_CANCELLED = "media_scanning_cancelled";

    private static final String EXTRA_FILES = "files";

    private static final String CHANNEL_ID_MEDIA_SCANNER = "media_scanner";
    private static final int NOTIFICATION_ID_MEDIA_SCANNER = 1735;

    public static void start(@NonNull Context ctx) {
        Intent intent = new Intent(ctx, MediaScanService.class).setAction(ACTION_SCAN_MEDIA);
        ContextCompat.startForegroundService(ctx, intent);
    }

    public static void start(@NonNull Context ctx, @NonNull ArrayList<String> targetFiles) {
        Intent intent = new Intent(ctx, MediaScanService.class)
                .setAction(ACTION_SCAN_MEDIA)
                .putExtra(EXTRA_FILES, targetFiles);
        ContextCompat.startForegroundService(ctx, intent);
    }

    public static void start(@NonNull Context ctx, @NonNull String targetFile) {
        ArrayList<String> targetFiles = new ArrayList<>(1);
        targetFiles.add(targetFile);
        start(ctx, targetFiles);
    }

    private static Intent newCancelIntent(Context context) {
        return new Intent(context, MediaScanService.class)
                .setAction(ACTION_CANCEL_SCAN_MEDIA);
    }

    private static class ScannerInfo {
        final int startId;
        final TimedScanner scanner;

        ScannerInfo(int startId, TimedScanner scanner) {
            this.startId = startId;
            this.scanner = scanner;
        }
    }

    private boolean mCreated = false;

    private NotificationManager mNotificationManager;

    private Thread mEngineThread;
    private Handler mEngineHandler;
    private Handler mMainHandler;

    private final SparseArray<ScannerInfo> mScanners = new SparseArray<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(LOG_TAG, "Creating service");

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        HandlerThread thread = new HandlerThread("MediaScan", Process.THREAD_PRIORITY_DEFAULT);
        thread.start();
        mEngineThread = thread;
        mEngineHandler = new Handler(thread.getLooper());
        mMainHandler = new Handler(Looper.getMainLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        // Do not forget to start foreground!
        Notification notification = createPreparationNotification();
        startForeground(NOTIFICATION_ID_MEDIA_SCANNER, notification);

        mCreated = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_MEDIA_SCANNER, getString(R.string.media_scanner_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.media_scanner_channel_desc));
            channel.setShowBadge(false);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.enableLights(false);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(@Nullable final Intent intent, int flags, final int startId) {
        final String action = intent != null ? intent.getAction() : null;

        if (DEBUG) Log.d(LOG_TAG, "Handle intent: " + action);

        if (ACTION_CANCEL_SCAN_MEDIA.equals(action)) {
            disposeAllScanners();

            stopForeground(true);
            stopSelf();

            return START_NOT_STICKY;
        } else if (ACTION_SCAN_MEDIA.equals(action)) {

            disposeAllScanners();

            handleScanPreparation(startId);

            if (intent.hasExtra(EXTRA_FILES)) {
                List<String> args = intent.getStringArrayListExtra(EXTRA_FILES);
                final List<String> targetFiles = args != null ? args : Collections.<String>emptyList();
                execCollectAndScanFiles(startId, false, targetFiles);
            } else {
                execCollectAndScanFiles(startId, true, null);
            }

            return START_REDELIVER_INTENT;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(LOG_TAG, "Destroying service");

        disposeAllScanners();

        stopForeground(true);

        mCreated = false;

        mNotificationManager = null;

        mEngineHandler.removeCallbacksAndMessages(null);
        mEngineHandler = null;

        mEngineThread.interrupt();
        mEngineThread = null;

        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
    }

    /**
     * Disposes and removes the scanner that is associated with the given <code>startId</code>.
     * @param startId id of the command
     */
    private void disposeScanner(int startId) {
        synchronized (mScanners) {
            ScannerInfo item = mScanners.get(startId);
            if (item != null) {
                item.scanner.dispose();
                mScanners.remove(startId);
            }
        }
    }

    /**
     * Disposes all active scanners and removes them then.
     */
    private void disposeAllScanners() {
        synchronized (mScanners) {
            for (int i = 0; i < mScanners.size(); i++) {
                ScannerInfo item = mScanners.valueAt(i);
                item.scanner.dispose();
            }
            mScanners.clear();
        }
    }

    /**
     * Collects and scans files on the device that need to be scanned.
     * If <code>fullRescan</code> is true then all files in the device will be collected.
     * Otherwise, only files from <code>targetFiles</code> (including their nested child files) will be collected, if not null.
     *
     * Note that collection is performed on the engine thread,
     * and then the async scanning is started on the main thread.
     * @param startId id of the command
     * @param fullRescan true if this should collect all the files on the device
     * @param targetFiles from which to collect files for scanning, ignored if <code>fullRescan</code> is true
     */
    private void execCollectAndScanFiles(
        final int startId,
        final boolean fullRescan,
        @Nullable final List<String> targetFiles
    ) {
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.w(LOG_TAG, "Collect all files to scan");
                List<String> files;
                try {
                    final AudioFileCollector collector = AudioFileCollector.get(MediaScanService.this);

                    if (fullRescan) {
                        files = collector.collectAll();
                    } else if (targetFiles != null) {
                        files = collector.collectFrom(targetFiles);
                    } else {
                        files = new ArrayList<>(0);
                    }
                } catch (SecurityException e) {
                    if (DEBUG) Log.e(LOG_TAG, "", e);
                    files = new ArrayList<>(0);
                }

                if (Thread.interrupted()) {
                    if (DEBUG) Log.w(LOG_TAG, "Engine thread is interrupted");
                    // thread interrupted => cancel scanning
                    return;
                }

                Handler h = mMainHandler;
                if (h != null) {
                    final List<String> paths = files;
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            scanAsync(startId, paths);
                        }
                    });
                }
            }
        };

        mEngineHandler.post(task);
    }

    /**
     * Starts an asynchronous scan of the given <code>files</code>.
     * This creates and saves a {@link ScannerInfo} that is associated with <code>startId</code>.
     * @param startId id of the command
     * @param files to scan
     */
    @MainThread
    private void scanAsync(final int startId, @NonNull final List<String> files) {
        if (!mCreated) {
            // Service is not created yet or destroyed already
            return;
        }

        // We need to pass the application context to avoid memory leak issues.
        // See https://stackoverflow.com/questions/5739140/mediascannerconnection-produces-android-app-serviceconnectionleaked
        final Context appContext = getApplicationContext();

        final TimedScanner.ScanCallback callback = new TimedScanner.ScanCallback() {
            @Override
            public void onScanStarted() {
                handleScanStarted(startId, files.size());
            }

            @Override
            public void onProgressChanged(int total, int progress) {
                handleProgressChanged(startId, total, progress);
            }

            @Override
            public void onScanCompleted() {
                handleScanCompleted(startId);
            }

            @Override
            public void onScanCancelled() {
                handleScanCancelled(startId);
            }
        };

        final TimedScanner scanner = TimedScanner.create(
                appContext, mMainHandler, files, SCAN_TIMEOUT, callback);

        final ScannerInfo info = new ScannerInfo(startId, scanner);
        synchronized (mScanners) {
            ScannerInfo oldInfo = mScanners.get(startId);
            if (oldInfo != null) {
                // abort old scanner
                oldInfo.scanner.dispose();
            }

            // start new scanner
            info.scanner.start();
            // put it for this start id
            mScanners.put(startId, info);
        }

        if (DEBUG) Log.d(LOG_TAG, "Async scan started for command ID " + startId);
    }

    //region Event handlers
    private void handleScanPreparation(int startId) {
        if (DEBUG) Log.d(LOG_TAG, String.format("Preparing: startId=%d. Created=%b", startId, mCreated));

        if (mNotificationManager != null) {
            Notification notification = createPreparationNotification();
            mNotificationManager.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification);
        }
    }

    private void handleScanStarted(int startId, int total) {
        if (DEBUG) Log.d(LOG_TAG, String.format("Scan started: startId=%d, total=%d. Created=%b", startId, total, mCreated));

        if (mCreated) {
            Intent statusIntent = new Intent(ACTION_MEDIA_SCANNING_STATUS)
                    .putExtra(EXTRA_MEDIA_SCANNING_STARTED, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
        }

        if (mNotificationManager != null) {
            Notification notification = createProgressNotification(total, 0);
            mNotificationManager.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification);
        }
    }

    private void handleProgressChanged(int startId, int total, int progress) {
        if (DEBUG) Log.d(LOG_TAG, String.format("Progress changed: startId=%d, total=%d, progress=%d. Created=%b", startId, total, progress, mCreated));

        if (mNotificationManager != null) {
            Notification notification = createProgressNotification(total, progress);
            mNotificationManager.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification);
        }
    }

    private void handleScanCompleted(int startId) {
        if (DEBUG) Log.d(LOG_TAG, String.format("Scan completed: startId=%d. Created=%b", startId, mCreated));

        if (mCreated) {
            Intent statusIntent = new Intent(ACTION_MEDIA_SCANNING_STATUS).
                    putExtra(EXTRA_MEDIA_SCANNING_COMPLETED, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
        }

        synchronized (mScanners) {
            mScanners.remove(startId);
        }

        stopSelf(startId);
    }

    private void handleScanCancelled(int startId) {
        if (DEBUG) Log.d(LOG_TAG, String.format("Scan cancelled: startId=%d. Created=%b", startId, mCreated));

        if (mCreated) {
            Intent statusIntent = new Intent(ACTION_MEDIA_SCANNING_STATUS)
                    .putExtra(EXTRA_MEDIA_SCANNING_CANCELLED, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(statusIntent);
        }

        synchronized (mScanners) {
            mScanners.remove(startId);
        }

        stopSelf(startId);
    }
    //endregion

    private Notification createPreparationNotification() {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_media_scan_preparing);

        final PendingIntent cancelPi = PendingIntent.getService(
                this, RC_CANCEL, newCancelIntent(this), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_cancel, cancelPi);

        remoteViews.setProgressBar(R.id.pb_progress, 0, 0, true);

        return new NotificationCompat.Builder(this, CHANNEL_ID_MEDIA_SCANNER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(getString(R.string.preparing_files_to_scan))
                .setCustomBigContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.ic_scan_file)
                .build();
    }

    private Notification createProgressNotification(int total, int progress) {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_media_scan_scanning);

        remoteViews.setTextViewText(R.id.tv_message, getString(R.string.scanning_media_storage));

        remoteViews.setTextViewText(R.id.tv_progress, progress + "/" + total);

        final PendingIntent cancelPi = PendingIntent.getService(
                this, RC_CANCEL, newCancelIntent(this), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_cancel, cancelPi);

        remoteViews.setProgressBar(R.id.pb_progress, total, progress, false);

        return new NotificationCompat.Builder(this, CHANNEL_ID_MEDIA_SCANNER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(getString(R.string.scanning_media_storage))
                .setCustomBigContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setSmallIcon(R.drawable.ic_scan_file)
                .build();
    }

}