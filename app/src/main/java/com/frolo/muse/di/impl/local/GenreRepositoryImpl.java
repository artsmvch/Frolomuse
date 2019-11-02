package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.GenreRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class GenreRepositoryImpl implements GenreRepository {

    private final static String[] SORT_ORDER_KEYS = {
            GenreQuery.Sort.BY_NAME
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                GenreQuery.Sort.BY_NAME);
    }

    private final Context mContext;
    private final Map<String, String> mSortOrders;

    public GenreRepositoryImpl(final Context context) {
        this.mContext = context;
        this.mSortOrders = new LinkedHashMap<String, String>(1, 1f) {{
            put(GenreQuery.Sort.BY_NAME,
                    context.getString(R.string.sort_by_name));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<Genre>> getAllItems() {
        return GenreQuery.queryAll(mContext.getContentResolver());
    }

    @Override
    public Flowable<List<Genre>> getAllItems(final String sortOrder) {
        return GenreQuery.queryAll(mContext.getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Genre>> getFilteredItems(final String filter) {
        return GenreQuery.queryAllFiltered(mContext.getContentResolver(), filter);
    }

    @Override
    public Flowable<Genre> getItem(final long id) {
        return GenreQuery.querySingle(mContext.getContentResolver(), id);
    }

    @Override
    public Single<Genre> findItemByName(final String name) {
        return GenreQuery.querySingleByName(
                mContext.getContentResolver(),
                name)
                .firstOrError();
    }

    @Override
    public Completable delete(Genre item) {
        return Del.deleteGenre(
                mContext.getContentResolver(),
                item);
    }

    @Override
    public Completable delete(Collection<Genre> items) {
        return Del.deleteGenres(
                mContext.getContentResolver(),
                items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Genre item) {
        return PlaylistHelper.addGenreToPlaylist(mContext.getContentResolver(), playlistId, item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Genre> items) {
        return PlaylistHelper.addItemsToPlaylist(mContext.getContentResolver(), playlistId, items);
    }

    @Override
    public Single<List<Song>> collectSongs(Genre item) {
        return SongQuery.queryForGenre(
                mContext.getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Genre> items) {
        return SongQuery.queryForGenres(
                mContext.getContentResolver(),
                items)
                .firstOrError();
    }

    @Override
    public Flowable<List<Genre>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> isFavourite(Genre item) {
        return Single.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> changeFavourite(Genre item) {
        return Single.error(new UnsupportedOperationException());
    }
}
