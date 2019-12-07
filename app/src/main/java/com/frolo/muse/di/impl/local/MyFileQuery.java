package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.frolo.muse.App;
import com.frolo.muse.BuildConfig;
import com.frolo.muse.db.AppMediaStore;
import com.frolo.muse.model.media.MyFile;

import java.io.File;
import java.io.FileFilter;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;


final class MyFileQuery {

    static final class Sort {
        static final String BY_FILENAME = "";

        private Sort() {
        }
    }

    private static final String PATH_STORAGE_ROOT = "/storage"; // absolute root
    private static final String PATH_EMULATED_ROOT = PATH_STORAGE_ROOT + "/emulated";
    private static final String PATH_EMULATED_0_ROOT = PATH_STORAGE_ROOT + "/emulated/0";

    /*Hidden files*/
    private static final Uri URI_HIDDEN_FILES = AppMediaStore.HiddenFiles.getContentUri();

    private static final String[] PROJECTION_HIDDEN_FILES =
            {
                    AppMediaStore.HiddenFiles.ABSOLUTE_PATH,
                    AppMediaStore.HiddenFiles.TIME_HIDDEN
            };

    private static final Query.Builder<MyFile> BUILDER_HIDDEN_FILES =
            new Query.Builder<MyFile>() {
                @Override
                public MyFile build(Cursor cursor, String[] projection) {
                    String absolutePath = cursor.getString(
                            cursor.getColumnIndex(PROJECTION_HIDDEN_FILES[0]));

                    try {
                        File javaFile = new File(absolutePath);
                        boolean isSongFile = guessIsAudio(absolutePath);
                        return new MyFile(javaFile, isSongFile);
                    } catch (Throwable err) {
                        if (BuildConfig.DEBUG) {
                            throw new RuntimeException(err);
                        }
                        return null;
                    }
                }
            };

    // we are only interested in audios and folders containing audios
    private static class AudioFileFilter implements FileFilter {
        final ContentResolver resolver;

        AudioFileFilter(ContentResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public boolean accept(File pathname) {
            if (!pathname.exists()) {
                // exclude files that do not exists
                return false;
            }

            if (pathname.isHidden()) {
                // exclude files that are hidden
                return false;
            }

            final File parent = pathname.getParentFile();
            if (parent != null && parent.equals(pathname)) {
                // Exclude files which are parents for themselves
                return false;
            }

            if (pathname.isDirectory()) {
                // if it's a folder we can check if it contains audio files
                return containsAudio(resolver, pathname);
            } else {
                // last chance to check if it's an audio file
                return guessIsAudio(pathname.getAbsolutePath());
            }
        }
    }

    private static class FoldersGoFirstComparator implements Comparator<File> {
        @Override public int compare(File o1, File o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            if (o1.isDirectory() && o2.isDirectory())
                return o1.compareTo(o2);
            if (o1.isDirectory()) return -1;
            if (o2.isDirectory()) return 1;
            return o1.compareTo(o2);
        }
    }

    private static boolean guessIsAudio(String path) {
        try {
            // StringIndexOutOfBoundsException can occur in some cases so I had to wrap it up into try-catch statement
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("audio");
        } catch (Exception exc) {
            return false;
        }
    }

    private static boolean containsAudio(ContentResolver cr, File file) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] { MediaStore.Audio.Media.DATA };
        String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[] { "%" + file.getAbsolutePath() + "/%" };
        // limit the query by 1 item so it will work faster???
        String sortOrder = MediaStore.Audio.Media.TITLE + " LIMIT 1";
        // Do NOT query with null projection as it will return all columns
        Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            boolean isDataPresent = cursor.getCount() > 0;
            cursor.close();
            return isDataPresent;
        }
        return false;
    }

    static MyFile getRootFile() {
        return new MyFile(new File(PATH_STORAGE_ROOT), false);
    }

    static List<MyFile> getRootFiles(Context context) {
        List<MyFile> roots = new ArrayList<>(2);

        final File defRoot = Environment.getExternalStorageDirectory();
        if (defRoot != null) {
            roots.add(new MyFile(defRoot, false));
        }

        final File sdCardRoot = SDCard.findSdCardPath(context);
        if (sdCardRoot != null) {
            roots.add(new MyFile(sdCardRoot, false));
        }

        return roots;
    }

    static Flowable<List<MyFile>> browse(final Context context, final MyFile myFile) {
        return Flowable.create(new FlowableOnSubscribe<List<MyFile>>() {
            @Override
            public void subscribe(final FlowableEmitter<List<MyFile>> emitter) {
                final String path = myFile.getJavaFile().getAbsolutePath();
                final MyFile target;
                if (path.equals(PATH_EMULATED_ROOT)) {
                    target = new MyFile(new File(PATH_EMULATED_0_ROOT), false);
                } else {
                    target = myFile;
                }

                if (!target.getJavaFile().isDirectory()) {
                    throw new IllegalArgumentException(
                            "Cannot browse not a directory: " + myFile);
                }

                final ContentResolver resolver = context.getContentResolver();
                final String[] PROJECTION_EMPTY = new String[0];

                final List<MyFile> result = new ArrayList<>();
                emitter.onNext(new ArrayList<>(result));
                //final FileFilter filter = new AudioFileFilter(context, target.getJavaFile());
                final FileFilter filter = new AudioFileFilter(context.getContentResolver());

                final File[] files = target.getJavaFile().listFiles();

                if (files != null) {
                    Comparator<File> comparator = new FoldersGoFirstComparator();
                    Arrays.sort(files, comparator);
                    for (File f : files) {
                        if (emitter.isCancelled()) {
                            return;
                        }

                        // Check if it's hidden
                        Cursor cursor = resolver.query(
                                URI_HIDDEN_FILES,
                                PROJECTION_EMPTY,
                                AppMediaStore.HiddenFiles.ABSOLUTE_PATH + " = ?",
                                new String[] { f.getAbsolutePath() },
                                null
                        );

                        boolean isHidden = cursor != null && cursor.getCount() > 0;

                        if (cursor != null) {
                            cursor.close();
                        }

                        if (!isHidden && filter.accept(f)) {
                            result.add(new MyFile(f, guessIsAudio(f.getAbsolutePath())));
                            emitter.onNext(new ArrayList<>(result));
                        }
                    }
                }

//                {
//                    final String fileToObserve = myFile.getJavaFile().getAbsolutePath();
//                    final int mask = FileObserver.DELETE;
//                    final FileObserver fileObserver = new FileObserver(fileToObserve, mask) {
//                        @Override
//                        public void onEvent(int event, String path) {
//                            if (path == null) {
//                                return;
//                            }
//
//                            if (event == FileObserver.DELETE) {
//                                for (int i = 0; i < result.size(); i++) {
//                                    if (result.get(i).getJavaFile().getAbsolutePath().equals(path)) {
//                                        result.remove(i);
//                                        emitter.onNext(new ArrayList<>(result));
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    };
//
//                    fileObserver.startWatching();
//
//                    emitter.setDisposable(Disposables.fromAction(new Action() {
//                        @Override
//                        public void run() {
//                            fileObserver.stopWatching();
//                        }
//                    }));
//                }

                emitter.onComplete();
            }
        }, BackpressureStrategy.LATEST);
    }

    static Flowable<List<MyFile>> getHiddenFiles(ContentResolver resolver) {
        return Query.query(
                resolver,
                URI_HIDDEN_FILES,
                PROJECTION_HIDDEN_FILES,
                null,
                null,
                null,
                BUILDER_HIDDEN_FILES
        );
    }

    static Completable setFileHidden(final ContentResolver resolver, final MyFile item, final boolean hidden) {
        return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() throws Exception {
                        if (hidden) {
                            ContentValues values = new ContentValues();
                            values.put(AppMediaStore.HiddenFiles.ABSOLUTE_PATH, item.getJavaFile().getAbsolutePath());
                            values.put(AppMediaStore.HiddenFiles.TIME_HIDDEN, System.currentTimeMillis());
                            Uri resultUri = resolver.insert(URI_HIDDEN_FILES, values);
                            if (resultUri == null) {
                                // Is it ok?
                            }
                        } else {
                            String selection = AppMediaStore.HiddenFiles.ABSOLUTE_PATH + " = ?";
                            String[] selectionArgs = new String[] { item.getJavaFile().getAbsolutePath() };
                            int deletedCount = resolver.delete(
                                    URI_HIDDEN_FILES,
                                    selection,
                                    selectionArgs
                            );
                            if (deletedCount == 0) {
                                throw new IllegalArgumentException(
                                        "Failed to delete the file from hidden. Perhaps he was no longer hidden."
                                );
                            }
                        }
                    }
                }
        );
    }

    private MyFileQuery() {
    }
}
