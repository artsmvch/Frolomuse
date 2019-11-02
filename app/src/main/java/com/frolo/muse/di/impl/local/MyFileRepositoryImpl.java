package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.MyFileRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class MyFileRepositoryImpl implements MyFileRepository {

    private final static String[] SORT_ORDER_KEYS = {
            MyFileQuery.Sort.BY_FILENAME
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                MyFileQuery.Sort.BY_FILENAME);
    }

    private final Context mContext;
    private final Map<String, String> mSortOrders =
            new LinkedHashMap<>(1, 1f);

    public MyFileRepositoryImpl(Context context) {
        this.mContext = context;
        mSortOrders.put(MyFileQuery.Sort.BY_FILENAME,
                context.getString(R.string.sort_by_filename));
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
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
    public Single<Boolean> isFavourite(MyFile item) {
        return Single.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> changeFavourite(MyFile item) {
        return Single.error(new UnsupportedOperationException());
    }

    @Override
    public Single<MyFile> getRootFile() {
        return Single.just(MyFileQuery.getRootFile());
    }

    @Override
    public Flowable<List<MyFile>> browse(final MyFile myFile) {
        return MyFileQuery.browse(mContext, myFile);
    }
}
