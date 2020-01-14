package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.PlaylistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class PlaylistRepositoryImpl
        implements PlaylistRepository {

    private final static String[] SORT_ORDER_KEYS = {
            PlaylistQuery.Sort.BY_NAME,
            PlaylistQuery.Sort.BY_DATE_ADDED,
            PlaylistQuery.Sort.BY_DATE_MODIFIED
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                PlaylistQuery.Sort.BY_NAME);
    }

    private final Context mContext;
    private final Map<String, String> mSortOrders;

    public PlaylistRepositoryImpl(final Context context) {
        this.mContext = context;
        mSortOrders = new LinkedHashMap<String, String>(3, 1f) {{
            put(PlaylistQuery.Sort.BY_NAME,
                    context.getString(R.string.sort_by_name));

            put(PlaylistQuery.Sort.BY_DATE_ADDED,
                    context.getString(R.string.sort_by_date_added));

            put(PlaylistQuery.Sort.BY_DATE_MODIFIED,
                    context.getString(R.string.sort_by_date_modified));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<Playlist>> getAllItems() {
        return PlaylistQuery.queryAll(mContext.getContentResolver());
    }

    @Override
    public Flowable<List<Playlist>> getAllItems(final String sortOrder) {
        return PlaylistQuery.queryAll(mContext.getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Playlist>> getFilteredItems(final String filter) {
        return PlaylistQuery.queryAllFiltered(mContext.getContentResolver(), filter);
    }

    @Override
    public Flowable<Playlist> getItem(final long id) {
        return PlaylistQuery.querySingle(mContext.getContentResolver(), id);
    }

    @Override
    public Completable delete(Playlist item) {
        return Del.deletePlaylist(
                mContext.getContentResolver(),
                item);
    }

    @Override
    public Completable delete(Collection<Playlist> items) {
        return Del.deletePlaylists(
                mContext.getContentResolver(),
                items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Playlist item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Playlist> items) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<List<Song>> collectSongs(Playlist item) {
        return SongQuery.queryForPlaylist(
                mContext.getContentResolver(),
                item,
                SongQuery.Sort.BY_PLAY_ORDER)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Playlist> items) {
        return SongQuery.queryForPlaylists(
                mContext.getContentResolver(),
                items)
                .firstOrError();
    }

    @Override
    public Single<Playlist> create(final String name) {
        return PlaylistQuery.create(
                mContext,
                mContext.getContentResolver(),
                name);
    }

    @Override
    public Single<Playlist> update(final Playlist playlist, final String newName) {
        return PlaylistQuery.update(
                mContext,
                mContext.getContentResolver(),
                playlist,
                newName);
    }

    @Override
    public Flowable<List<Playlist>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<Boolean> isFavourite(Playlist item) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable changeFavourite(Playlist item) {
        return Completable.error(new UnsupportedOperationException());
    }
}
