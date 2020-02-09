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
import android.util.SparseArray;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.frolo.muse.R;
import com.frolo.muse.Trace;

import java.util.ArrayList;
import java.util.List;


public class MediaScanService extends Service {
    private static final String LOG_TAG = MediaScanService.class.getSimpleName();

    private static final int RC_CANCEL = 3731;

    // used for broadcasting
    public static final String ACTION_MEDIA_SCANNING_STATUS = "com.frolo.muse.mediascan.ACTION_MEDIA_SCANNING_STATUS";
    private static final String ACTION_SCAN_MEDIA = "com.frolo.muse.mediascan.ACTION_SCAN_MEDIA";
    private static final String ACTION_CANCEL_SCAN_MEDIA = "com.frolo.muse.mediascan.ACTION_CANCEL_SCAN_MEDIA";

    public static final String EXTRA_MEDIA_SCANNING_STARTED = "media_scanning_started";
    public static final String EXTRA_MEDIA_SCANNING_COMPLETED = "media_scanning_completed";
    public static final String EXTRA_MEDIA_SCANNING_CANCELLED = "media_scanning_cancelled";

    private static final String EXTRA_FILES = "files";

    private static final String CHANNEL_ID_MEDIA_SCANNER = "media_scanner";
    private static final int NOTIFICATION_ID_MEDIA_SCANNER = 1735;

    public static Intent newIntent(Context context, @Nullable ArrayList<String> files) {
        return new Intent(context, MediaScanService.class)
                .setAction(ACTION_SCAN_MEDIA)
                .putExtra(EXTRA_FILES, files);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MediaScanService.class)
                .setAction(ACTION_SCAN_MEDIA);
    }

    private static Intent newCancelIntent(Context context) {
        return new Intent(context, MediaScanService.class)
                .setAction(ACTION_CANCEL_SCAN_MEDIA);
    }

    private static @Nullable List<String> getFilesExtra(Intent intent) {
        return intent.getStringArrayListExtra(EXTRA_FILES);
    }

    private static class ScannerInfo {
        final int mStartId;
        final List<String> mFiles;
        final TimedScanner mScanner;
        ScannerInfo(int startId, List<String> files, TimedScanner scanner) {
            this.mStartId = startId;
            this.mFiles = files;
            this.mScanner = scanner;
        }
    }

    private NotificationManager mNotificationManager;
    private Thread mEngineThread;
    private Handler mEngineHandler;
    private Handler mMainHandler;
    private Runnable mNotifyPreparingFilesCallback = new Runnable() {
        @Override
        public void run() {
            if (mNotificationManager != null) {
                Notification notification = createNotification(getString(R.string.scanning_media_storage));
                mNotificationManager.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification);
            }
        }
    };
    private Runnable mNotifyScanningFilesCallback = new Runnable() {
        @Override
        public void run() {
            if (mNotificationManager != null) {
                Notification notification = createNotification(getString(R.string.scanning_media_storage));
                mNotificationManager.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification);
            }
        }
    };
    private final SparseArray<ScannerInfo> mScanners = new SparseArray<>();

    private Notification createNotification(String message) {
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_media_scan);
        remoteViews.setTextViewText(R.id.tv_message, message);
        final PendingIntent cancelPi = PendingIntent.getService(this, RC_CANCEL, newCancelIntent(this), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_cancel, cancelPi);
        return new NotificationCompat.Builder(this, CHANNEL_ID_MEDIA_SCANNER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCustomContentView(remoteViews)
                .setSmallIcon(R.drawable.ic_scan_file)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Trace.d(LOG_TAG, "Creating service");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        HandlerThread thread = new HandlerThread("MediaScan", Process.THREAD_PRIORITY_DEFAULT);
        thread.start();
        mEngineThread = thread;
        mEngineHandler = new Handler(thread.getLooper());
        mMainHandler = new Handler(Looper.getMainLooper());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        Notification notification = createNotification(getString(R.string.preparing_files_to_scan));
        startForeground(NOTIFICATION_ID_MEDIA_SCANNER, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager != null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_MEDIA_SCANNER, getString(R.string.media_scanner_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
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
        if (action != null && action.equals(ACTION_CANCEL_SCAN_MEDIA)) {
            Trace.d(LOG_TAG, "Handling intent: CANCEL_SCAN_MEDIA");
            abortAllScanners();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else if (action != null && action.equals(ACTION_SCAN_MEDIA)) {
            Trace.d(LOG_TAG, "Handling intent: SCAN_MEDIA");
            mMainHandler.post(mNotifyPreparingFilesCallback);
            mEngineHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<String> files = getFilesExtra(intent);
                    if (files == null) {
                        Trace.d(LOG_TAG, "No files extra in intent. Collecting files for scanning");
                        try {
                            files = AudioFileCollector.get(MediaScanService.this).collect();
                        } catch (SecurityException e) {
                            Trace.e(LOG_TAG, e);
                            files = new ArrayList<>(0);
                        }
                    }
                    if (Thread.interrupted()) {
                        Trace.w(LOG_TAG, "Engine thread is interrupted");
                        // thread interrupted => cancel scanning
                        return;
                    }
                    Trace.d(LOG_TAG, "Scanning files...");
                    scanAsync(files, startId);
                    Handler h = mMainHandler;
                    if (h != null) {
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mNotificationManager != null) {
                                    Notification notification = createNotification(getString(R.string.scanning_media_storage));
                                    mNotificationManager.notify(NOTIFICATION_ID_MEDIA_SCANNER, notification);
                                }
                            }
                        });
                    } else {
                        Trace.w(LOG_TAG, "Main handler is null");
                    }
                }
            });
            return START_REDELIVER_INTENT;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Trace.d(LOG_TAG, "Destroying service");
        stopForeground(true);

        mNotificationManager = null;

        mEngineHandler.removeCallbacksAndMessages(null);
        mEngineHandler = null;

        mEngineThread.interrupt();
        mEngineThread = null;

        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
    }

    private void abortAllScanners() {
        synchronized (mScanners) {
            for (int i = 0; i < mScanners.size(); i++) {
                ScannerInfo item = mScanners.valueAt(i);
                item.mScanner.dispose();
            }
            mScanners.clear();
        }
    }

    private void scanAsync(final List<String> files, final int startId) {
        //String[] strArr = (String[]) files.toArray(new String[files.size()]);
        final Context appContext = getApplicationContext();
        // We need to pass the application context to avoid memory leak issues.
        // See https://stackoverflow.com/questions/5739140/mediascannerconnection-produces-android-app-serviceconnectionleaked
        final TimedScanner scanner = TimedScanner.create(appContext, mEngineHandler, mMainHandler, files, 5_000, new TimedScanner.ScanCallback() {
            @Override
            public void onScanStarted() {
                Intent statusIntent = new Intent(ACTION_MEDIA_SCANNING_STATUS).putExtra(EXTRA_MEDIA_SCANNING_STARTED, true);
                LocalBroadcastManager.getInstance(appContext).sendBroadcast(statusIntent);
            }

            @Override
            public void onProgressChanged(int progress, int total) {

            }

            @Override
            public void onScanCompleted() {
                Intent statusIntent = new Intent(ACTION_MEDIA_SCANNING_STATUS).putExtra(EXTRA_MEDIA_SCANNING_COMPLETED, true);
                LocalBroadcastManager.getInstance(appContext).sendBroadcast(statusIntent);
                synchronized (mScanners) {
                    mScanners.remove(startId);
                }
                stopSelf(startId);
            }

            @Override
            public void onScanCancelled() {
                Intent statusIntent = new Intent(ACTION_MEDIA_SCANNING_STATUS).putExtra(EXTRA_MEDIA_SCANNING_CANCELLED, true);
                LocalBroadcastManager.getInstance(appContext).sendBroadcast(statusIntent);
                synchronized (mScanners) {
                    mScanners.remove(startId);
                }
                stopSelf(startId);
            }
        });
        final ScannerInfo info = new ScannerInfo(startId, files, scanner);
        synchronized (mScanners) {
            ScannerInfo oldInfo = mScanners.get(startId);
            if (oldInfo != null) {
                // abort old scanner
                oldInfo.mScanner.dispose();
            }

            // start new scanner
            info.mScanner.start();
            // put it for this start id
            mScanners.put(startId, info);
        }
    }
}