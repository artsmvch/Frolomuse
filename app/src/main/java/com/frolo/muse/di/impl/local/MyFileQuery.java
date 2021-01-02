package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.db.AppMediaStore;
import com.frolo.muse.model.media.MyFile;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;


final class MyFileQuery {

    static final class Sort {
        static final String BY_FILENAME = "filename";
        static final String BY_DATE_MODIFIED = "date_modified";

        static final Comparator<MyFile> COMPARATOR_BY_FILENAME = new Comparator<MyFile>() {
            @Override
            public int compare(MyFile o1, MyFile o2) {
                if (o1 == null && o2 == null) return 0;

                if (o1 == null) return -1;

                if (o2 == null) return 1;

                return o1.getJavaFile().compareTo(o2.getJavaFile());
            }
        };

        static final Comparator<MyFile> COMPARATOR_BY_DATE_MODIFIED = new Comparator<MyFile>() {
            @Override
            public int compare(MyFile o1, MyFile o2) {
                if (o1 == null && o2 == null) return 0;

                if (o1 == null) return -1;

                if (o2 == null) return 1;

                final long lastModified1 = o1.getJavaFile().lastModified();
                final long lastModified2 = o2.getJavaFile().lastModified();

                // TODO: does that truly sort files by date modified?
                return Long.compare(lastModified1, lastModified2);
            }
        };

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

    /**
     * Returns the filepath to the root file according to the version of OS the app is running on.
     * @return the filepath to the root file
     */
    static String getRootFilePath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return PATH_EMULATED_0_ROOT;
        }
        return PATH_STORAGE_ROOT;
    }

    static MyFile getRootFile() {
        return new MyFile(new File(getRootFilePath()), false);
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

    /*Creates a flowable that emits lists of deleted files*/
    @Deprecated
    private static Flowable<List<MyFile>> observeFileDeletion(
            Context context,
            final MyFile myFile
    ) {
        return Flowable.create(new FlowableOnSubscribe<List<MyFile>>() {
            @Override
            public void subscribe(final FlowableEmitter<List<MyFile>> emitter) throws Exception {
                if (!emitter.isCancelled()) {
                    final File fileToObserve = myFile.getJavaFile();
                    final int mask = FileObserver.DELETE;
                    final FileObserver fileObserver = new FileObserver(fileToObserve.getAbsolutePath(), mask) {
                        @Override
                        public void onEvent(int event, String path) {
                            if (path == null) {
                                return;
                            }

                            if (event == FileObserver.DELETE) {
                                File javaFile = new File(path);
                                boolean isSong = guessIsAudio(path);
                                List<MyFile> files = Collections.singletonList(new MyFile(javaFile, isSong));
                                emitter.onNext(files);
                            }
                        }
                    };

                    fileObserver.startWatching();

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            fileObserver.stopWatching();
                        }
                    }));
                }

                if (!emitter.isCancelled()) {
                    emitter.onNext(Collections.<MyFile>emptyList());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    /*Creates a flowable that emits lists of deleted files*/
    private static Flowable<List<File>> observeFileDeletion_Internal(
            Context context,
            final MyFile myFile
    ) {
        return Flowable.create(new FlowableOnSubscribe<List<File>>() {
            @Override
            public void subscribe(final FlowableEmitter<List<File>> emitter) {
                if (!emitter.isCancelled()) {
                    final FileDeletion.Watcher w = new FileDeletion.Watcher() {
                        @Override
                        public void onFilesDeleted(List<File> files) {
                            emitter.onNext(files);
                        }
                    };

                    FileDeletion.startWatching(w);

                    emitter.setDisposable(Disposables.fromAction(new Action() {
                        @Override
                        public void run() {
                            FileDeletion.stopWatching(w);
                        }
                    }));
                }

                if (!emitter.isCancelled()) {
                    emitter.onNext(Collections.<File>emptyList());
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    private static Flowable<List<MyFile>> browse_Internal(final Context context, final MyFile myFile) {
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

                        if (filter.accept(f)) {
                            result.add(new MyFile(f, guessIsAudio(f.getAbsolutePath())));
                            emitter.onNext(new ArrayList<>(result));
                        }
                    }
                }

                //emitter.onComplete();
            }
        }, BackpressureStrategy.LATEST);
    }

    static Flowable<List<MyFile>> browse(final Context context, final MyFile myFile, final String sortOrderKey) {
        List<Flowable<? extends List<? extends Serializable>>> sources = Arrays.asList(
                browse_Internal(context, myFile),
                getHiddenFiles(context.getContentResolver()),
                observeFileDeletion_Internal(context, myFile)
        );

        return Flowable.combineLatest(
                sources,
                new Function<Object[], List<MyFile>>() {
                    @Override
                    public List<MyFile> apply(Object[] objects) {
                        List<MyFile> browsedMyFiles = (List<MyFile>) objects[0];
                        List<MyFile> hiddenMyFiles = (List<MyFile>) objects[1];
                        List<File> deletedFiles = (List<File>) objects[2];

                        List<MyFile> result = new ArrayList<>(browsedMyFiles);

                        // Clean up from the hidden files first
                        result.removeAll(hiddenMyFiles);

                        // Then remove all the items that have been deleted
                        for (int i = 0; i < result.size(); i++) {
                            MyFile item = result.get(i);

                            for (File f : deletedFiles) {
                                if (item.getJavaFile().equals(f)) {
                                    result.remove(i--);
                                    break;
                                }
                            }
                        }

                        return result;
                    }
                }
        ).map(new Function<List<MyFile>, List<MyFile>>() {
            @Override
            public List<MyFile> apply(List<MyFile> myFiles) throws Exception {
                if (Objects.equals(sortOrderKey, Sort.BY_FILENAME)) {
                    Collections.sort(myFiles, Sort.COMPARATOR_BY_FILENAME);
                }

                if (Objects.equals(sortOrderKey, Sort.BY_DATE_MODIFIED)) {
                    Collections.sort(myFiles, Sort.COMPARATOR_BY_DATE_MODIFIED);
                }

                return myFiles;
            }
        });
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
