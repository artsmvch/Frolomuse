package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.SongRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class SongRepositoryImpl
        implements SongRepository {

    private final static String[] SORT_ORDER_KEYS = {
            SongQuery.Sort.BY_TITLE,
            SongQuery.Sort.BY_ALBUM,
            SongQuery.Sort.BY_ARTIST,
            SongQuery.Sort.BY_DURATION
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                SongQuery.Sort.BY_TITLE);
    }

    private final Context mContext;
    private final Map<String, String> mSortOrders;

    public SongRepositoryImpl(final Context context) {
        this.mContext = context;
        mSortOrders = new LinkedHashMap<String, String>(4, 1f) {{
            put(SongQuery.Sort.BY_TITLE,
                    context.getString(R.string.sort_by_name));

            put(SongQuery.Sort.BY_ALBUM,
                    context.getString(R.string.sort_by_album));

            put(SongQuery.Sort.BY_ARTIST,
                    context.getString(R.string.sort_by_artist));

            put(SongQuery.Sort.BY_DURATION,
                    context.getString(R.string.sort_by_duration));
        }};
    }

    protected final Context getContext() {
        return mContext;
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<Song>> getAllItems() {
        return SongQuery.queryAll(mContext.getContentResolver());
    }

    @Override
    public Flowable<List<Song>> getAllItems(final String sortOrder) {
        return SongQuery.queryAll(mContext.getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Song>> getFilteredItems(final String filter) {
        return SongQuery.queryAllFiltered(mContext.getContentResolver(), filter);
    }

    @Override
    public Flowable<Song> getItem(final long id) {
        return SongQuery.querySingle(mContext.getContentResolver(), id);
    }

    @Override
    public Completable delete(Song item) {
        return Del.deleteSong(
                mContext.getContentResolver(),
                item);
    }

    @Override
    public Completable delete(Collection<Song> items) {
        return Del.deleteSongs(
                mContext.getContentResolver(),
                items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Song item) {
        return PlaylistHelper.addSongToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Song> items) {
        return PlaylistHelper.addItemsToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                items);
    }

    @Override
    public Single<List<Song>> collectSongs(final Song item) {
        return Single.just(Collections.singletonList(item));
    }

    @Override
    public Single<List<Song>> collectSongs(final Collection<Song> items) {
        return Single.just((List<Song>) new ArrayList<>(items));
    }

    @Override
    public Flowable<List<Song>> getAllFavouriteItems() {
        return SongQuery.queryAllFavourites(mContext.getContentResolver());
    }

    @Override
    public Single<Song> getSong(final String path) {
        return SongQuery.querySingleByPath(
                mContext.getContentResolver(),
                path)
                .firstOrError();
    }

    @Override
    public Single<Song> update(
            final Song song,
            final String newTitle,
            final String newAlbum,
            final String newArtist,
            final String newGenre) {

        return SongQuery.update(
                mContext.getContentResolver(),
                song,
                newTitle,
                newAlbum,
                newArtist,
                newGenre)
                .andThen(getItem(song.getId()))
                .firstOrError();
    }

    @Override
    public Flowable<List<Song>> getSongsFromAlbum(final Album album, String sortOrder) {
        return SongQuery.queryForAlbum(mContext.getContentResolver(), album, sortOrder);
    }

    @Override
    public Flowable<List<Song>> getSongsFromArtist(final Artist artist, String sortOrder) {
        return SongQuery.queryForArtist(mContext.getContentResolver(), artist, sortOrder);
    }

    @Override
    public Flowable<List<Song>> getSongsFromGenre(final Genre genre, String sortOrder) {
        return SongQuery.queryForGenre(mContext.getContentResolver(), genre, sortOrder);
    }

    @Override
    public Flowable<List<Song>> getSongsFromPlaylist(final Playlist playlist, String sortOrder) {
        return SongQuery.queryForPlaylist(mContext.getContentResolver(), playlist, sortOrder);
    }

    @Override
    public Flowable<List<Song>> getRecentlyAddedSongs(final long dateAdded) {
        return SongQuery.queryRecentlyAdded(mContext.getContentResolver(), dateAdded);
    }

    @Override
    public Flowable<Boolean> isFavourite(final Song item) {
        return SongQuery.isFavourite(mContext.getContentResolver(), item);
    }

    @Override
    public Completable changeFavourite(final Song item) {
        return SongQuery.changeFavourite(mContext.getContentResolver(), item);
    }
}
