package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.AlbumRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class AlbumRepositoryImpl implements AlbumRepository {

    private final static String[] SORT_ORDER_KEYS = {
            AlbumQuery.Sort.BY_ALBUM,
            AlbumQuery.Sort.BY_NUMBER_OF_SONGS
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                AlbumQuery.Sort.BY_ALBUM);
    }

    private final Context mContext;
    private final Map<String, String> mSortOrders;

    public AlbumRepositoryImpl(final Context context) {
        this.mContext = context;
        this.mSortOrders = new LinkedHashMap<String, String>(2, 1f) {{
            put(AlbumQuery.Sort.BY_ALBUM,
                    context.getString(R.string.sort_by_name));

            put(AlbumQuery.Sort.BY_NUMBER_OF_SONGS,
                    context.getString(R.string.sort_by_number_of_songs));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<Album>> getAllItems() {
        return AlbumQuery.queryAll(mContext.getContentResolver());
    }

    @Override
    public Flowable<List<Album>> getAllItems(final String sortOrder) {
        return AlbumQuery.queryAll(mContext.getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Album>> getFilteredItems(final String filter) {
        return AlbumQuery.queryAllFiltered(mContext.getContentResolver(), filter);
    }

    @Override
    public Flowable<Album> getItem(final long id) {
        return AlbumQuery.querySingle(mContext.getContentResolver(), id);
    }

    @Override
    public Completable delete(Album item) {
        return Del.deleteAlbum(
                mContext.getContentResolver(),
                item);
    }

    @Override
    public Completable delete(Collection<Album> items) {
        return Del.deleteAlbums(
                mContext.getContentResolver(),
                items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Album item) {
        return PlaylistHelper.addAlbumToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Album> items) {
        return PlaylistHelper.addItemsToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                items);
    }

    @Override
    public Single<List<Song>> collectSongs(Album item) {
        return SongQuery.queryForAlbum(
                mContext.getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Album> items) {
        return SongQuery.queryForAlbums(
                mContext.getContentResolver(),
                items)
                .firstOrError();
    }

    @Override
    public Flowable<List<Album>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> isFavourite(Album item) {
        return Single.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> changeFavourite(Album item) {
        return Single.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<List<Album>> getAlbumsOfArtist(final Artist artist) {
        return AlbumQuery.queryForArtist(
                mContext.getContentResolver(),
                artist.getId());
    }

    @Override
    public Completable updateArt(final long albumId, final String filepath) {
        return AlbumQuery.updateAlbumArtPath(
                mContext.getContentResolver(),
                albumId,
                filepath);
    }
}
