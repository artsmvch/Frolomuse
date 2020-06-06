package com.frolo.muse.di.impl.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.R;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.MyFileRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Action;


public class MyFileRepositoryImpl implements MyFileRepository {

    /*Prefs*/
    private static final String PREFS_NAME = BuildConfig.APPLICATION_ID + ".file";

    private static final String KEY_DEFAULT_FOLDER_PATH = "default_folder_path";

    private final static String[] SORT_ORDER_KEYS = {
        MyFileQuery.Sort.BY_FILENAME
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, MyFileQuery.Sort.BY_FILENAME);
    }

    private final Context mContext;
    private final List<SortOrder> mSortOrders;

    private final SharedPreferences mPrefs;

    public MyFileRepositoryImpl(Context context) {
        this.mContext = context;
        mSortOrders = new ArrayList<SortOrder>(1) {{
            add(new SortOrderImpl(mContext, MyFileQuery.Sort.BY_FILENAME, R.string.sort_by_filename));
        }};

        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public Single<List<SortOrder>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<MyFile>> getAllItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<List<MyFile>> getAllItems(String sortOrder) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<List<MyFile>> getFilteredItems(String filter) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<MyFile> getItem(long id) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable delete(final MyFile item) {
        return Del.deleteMyFile(
                mContext.getContentResolver(),
                item);
    }

    @Override
    public Completable delete(Collection<MyFile> items) {
        return Del.deleteMyFiles(
                mContext.getContentResolver(),
                items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, MyFile item) {
        return PlaylistHelper.addMyFileToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                item);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<MyFile> items) {
        return PlaylistHelper.addItemsToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                items);
    }

    @Override
    public Single<List<Song>> collectSongs(MyFile item) {
        return SongQuery.queryForMyFile(
                mContext.getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<MyFile> items) {
        return SongQuery.queryForMyFiles(
                mContext.getContentResolver(),
                items)
                .firstOrError();
    }

    @Override
    public Flowable<List<MyFile>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<Boolean> isFavourite(MyFile item) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable changeFavourite(MyFile item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<MyFile> getRootFile() {
        return Single.just(MyFileQuery.getRootFile());
    }

    @Override
    public Single<MyFile> getDefaultFolder() {
        return Single.fromCallable(new Callable<MyFile>() {
            @Override
            public MyFile call() throws Exception {
                String defaultFolderPath = mPrefs.getString(KEY_DEFAULT_FOLDER_PATH, null);
                if (defaultFolderPath == null) {
                    throw new NullPointerException();
                }
                File file = new File(defaultFolderPath);
                if (!file.exists()) {
                    throw new IllegalStateException("File does not exist");
                }
                if (file.isHidden()) {
                    throw new IllegalStateException("File is hidden");
                }
                if (!file.isDirectory()) {
                    throw new IllegalStateException("File is not a directory");
                }
                return new MyFile(file, false);
            }
        }).onErrorResumeNext(getRootFile());
    }

    @Override
    public Completable setDefaultFolder(final MyFile folder) {
        return Completable.fromAction(new Action() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void run() throws Exception {
                File file = folder.getJavaFile();
                if (!file.exists()) {
                    throw new IllegalStateException("File does not exist");
                }
                if (file.isHidden()) {
                    throw new IllegalStateException("File is hidden");
                }
                if (!file.isDirectory()) {
                    throw new IllegalStateException("File is not a directory");
                }

                String filePath = file.getAbsolutePath();
                mPrefs.edit().putString(KEY_DEFAULT_FOLDER_PATH, filePath).commit();
            }
        });
    }

    @Override
    public Flowable<List<MyFile>> browse(final MyFile myFile) {
        return MyFileQuery.browse(mContext, myFile);
    }

    @Override
    public Flowable<List<MyFile>> getHiddenFiles() {
        return MyFileQuery.getHiddenFiles(mContext.getContentResolver());
    }

    @Override
    public Completable setFileHidden(MyFile item, boolean hidden) {
        return MyFileQuery.setFileHidden(
                mContext.getContentResolver(),
                item,
                hidden
        );
    }

    @Override
    public Single<Boolean> isShortcutSupported(MyFile item) {
        return Shortcuts.isShortcutSupported(mContext, item);
    }

    @Override
    public Completable createShortcut(MyFile item) {
        return Shortcuts.createMyFileShortcut(mContext, item);
    }

}
