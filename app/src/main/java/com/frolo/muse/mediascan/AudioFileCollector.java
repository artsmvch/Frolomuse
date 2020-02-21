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

        collectAudioFiles(absolutePath, list);
        collectAudioFiles(absolutePath + "/", list);

        distinctByMediaStore(null, list);

        return list;
    }

    /**
     * Collects all files that need to be scanned in the hierarchy of all <code>targetFiles</code>.
     * NOTE: this should be called on a worker thread.
     * @param targetFiles where to collect files for scanning
     * @return audio files to scan.
     */
    @WorkerThread
    List<String> collectFrom(@NonNull List<String> targetFiles) {
        final List<String> list = new ArrayList<>();
        for (String filepath : targetFiles) {
            collectAudioFiles(filepath, list);
            distinctByMediaStore(filepath, list);
        }
        return list;
    }

    /**
     * Collects all audio files in the <code>parent</code> hierarchy.
     * This also verifies that <code>parent</code> is not hidden, is a directory and cannot be skipped for scanning.
     * NOTE: the search is recursive.
     * @param dst to collect files to it
     * @param parent from which the search starts
     */
    private void collectAudioFiles(String parent, List<String> dst) {
        File file = new File(parent);
        if (!file.isHidden() && file.isDirectory() && !canSkipScanning(file)) {
            File[] listFiles = file.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File childFile : listFiles) {
                    if (childFile.isDirectory()) {
                        // recursively searching for audio files in child file
                        collectAudioFiles(childFile.getAbsolutePath(), dst);
                    } else if (isAudioFile(childFile.getAbsolutePath())) {
                        // it's an audio file, let's add it
                        dst.add(childFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * This adds any file found in the MediaStore but absent in the collection.
     * This removes any file found in the MediaStore and that is in the collection already.
     *
     * Clients may specify <code>parent</code> as common parent
     * to narrow down the selection of files from the MediaStore that need to be checked.
     * If <code>parent</code> is null, then all the files on the device will be checked.
     *
     * @param parent to narrow down the selection of files from the MediaStore, or null if all files in the MediaStore should be checked.
     * @param audioFiles to verify
     */
    private void distinctByMediaStore(@Nullable String parent, List<String> audioFiles) {
        final ContentResolver cr = mContext.getContentResolver();
        final String[] projection = new String[] { MediaStore.Audio.Media.DATA };

        final String selection;
        final String[] selectionArgs;
        if (parent != null) {
            // if fromPath is not null, then we can use 'where' condition
            selection = MediaStore.Audio.Media.DATA + " LIKE ?";
            selectionArgs = new String[] { "%" + parent + "%" };
        } else {
            selection = null;
            selectionArgs = null;
        }

        final Cursor cursor =
                cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);

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
