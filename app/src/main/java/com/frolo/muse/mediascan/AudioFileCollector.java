package com.frolo.muse.mediascan;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.frolo.muse.BuildConfig;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper class for collecting audio files that have not yet been scanned on the device.
 */
final class AudioFileCollector {
    private Context mContext;

    static AudioFileCollector get(@NonNull Context context) {
        return new AudioFileCollector(context);
    }

    private AudioFileCollector(Context context) {
        this.mContext = context;
    }

    private void logError(Throwable err) {
        if (BuildConfig.DEBUG) Log.e("AudioFileCollector", "", err);
    }

    /**
     * Collects all audio files on the device that need to be scanned.
     * NOTE: this should be called on a worker thread.
     * @return audio files to scan.
     */
    @WorkerThread
    List<String> collectAll() {
        final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final List<String> list = new ArrayList<>();
        collectAudioFiles(list, absolutePath);
        collectAudioFiles(list, absolutePath + "/");
        checkWithMediaStore(list);
        return list;
    }

    /**
     * Collects audio files from the given <code>files</code> (including their nested child files) that need to be scanned.
     * NOTE: this should be called on a worker thread.
     * @return audio files within the given <code>files</code> to scan.
     */
    @WorkerThread
    List<String> collectFrom(@NonNull List<String> files) {
        final List<String> list = new ArrayList<>();
        for (String file : files) {
            collectAudioFiles(list, file);
        }
        return list;
    }

    /**
     * Searches for all audio files that are in the given <code>startPath</code> hierarchy.
     * This also verifies that <code>starPath</code> is not hidden, is a directory and cannot be skipped for scanning.
     * NOTE: the search is recursive.
     * @param dst to collect files to it
     * @param startPath file from which the search starts
     */
    private void collectAudioFiles(List<String> dst, String startPath) {
        File file = new File(startPath);
        if (!file.isHidden() && file.isDirectory() && !canSkipScanning(file)) {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File childFile : listFiles) {
                    if (childFile.isDirectory()) {
                        // recursively searching for audio files in child file
                        collectAudioFiles(dst, childFile.getAbsolutePath());
                    } else if (isAudioFile(childFile.getAbsolutePath())) {
                        // it's an audio file, let's add it
                        dst.add(childFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Checks <code>audioFiles</code> according to the MediaStore on the device.
     * All files that the MediaStore knows are considered as scanned.
     * Adds any file found in the MediaStore but absent in the collection.
     * Removes any file found in the MediaStore and that is in the collection already.
     * @param audioFiles to check
     */
    private void checkWithMediaStore(List<String> audioFiles) {
        final String[] projection = new String[] { "_data" };
        final ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            String string = cursor.getString(cursor.getColumnIndex(projection[0]));
                            if (audioFiles.contains(string)) {
                                // this files is identified by MediaStore already, no need to scan it again. Remove it from the collection!
                                audioFiles.remove(string);
                            } else {
                                // this files is not in the collection, it may be deleted already, so need to scan it. Add it to the collection!
                                audioFiles.add(string);
                            }
                        } catch (Throwable e) {
                            logError(e);
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Checks if we can skip scanning the given <code>file</code>.
     * @param file to test
     * @return true if there is no need to scan the file, false - otherwise
     */
    private boolean canSkipScanning(@Nullable File file) {
        return new File(file, ".nomedia").exists();
    }

    /**
     * Checks if <code>filepath</code> is an audio file.
     * @param filepath to test
     * @return true if the file is an audio file, false - otherwise
     */
    private boolean isAudioFile(@Nullable String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            return false;
        }
        String type = null;
        // Not sure why we do this
        String replaceAll = filepath.replaceAll("[#]", "");
        try {
            type = URLConnection.guessContentTypeFromName(replaceAll);
        } catch (Throwable err) {
            logError(err);
        }
        return type != null && type.indexOf("audio") == 0;
    }
}
