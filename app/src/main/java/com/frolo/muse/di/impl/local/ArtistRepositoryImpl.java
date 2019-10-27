package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.ArtistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class ArtistRepositoryImpl implements ArtistRepository {

    private final Context mContext;
    private final Map<String, String> mSortOrders;

    public ArtistRepositoryImpl(final Context context) {
        this.mContext = context;
        this.mSortOrders = new LinkedHashMap<String, String>(3, 1f) {{
            put(ArtistQuery.Sort.BY_ARTIST,
                    context.getString(R.string.sort_by_name));

            put(ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS,
                    context.getString(R.string.sort_by_number_of_albums));

            put(ArtistQuery.Sort.BY_NUMBER_OF_TRACKS,
                    context.getString(R.string.sort_by_number_of_tracks));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Flowable<List<Artist>> getAllItems() {
        return ArtistQuery.queryAll(mContext.getContentResolver());
    }

    @Override
    public Flowable<List<Artist>> getAllItems(final String sortOrder) {
        return ArtistQuery.queryAll(
                mContext.getContentResolver(),
                sortOrder);
    }

    @Override
    public Flowable<List<Artist>> getFilteredItems(final String filter) {
        return ArtistQuery.queryAllFiltered(
                mContext.getContentResolver(),
                filter);
    }

    @Override
    public Flowable<Artist> getItem(final long id) {
        return ArtistQuery.querySingle(mContext.getContentResolver(), id);
    }

    @Override
    public Completable delete(Artist item) {
        return Del.deleteArtist(mContext.getContentResolver(), item);
    }

    @Override
    public Completable delete(Collection<Artist> items) {
        return Del.deleteArtists(
                mContext.getContentResolver(),
                items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Artist item) {
        return PlaylistHelper.addArtistToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Artist> items) {
        return PlaylistHelper.addItemsToPlaylist(
                mContext.getContentResolver(),
                playlistId,
                items);
    }

    @Override
    public Single<List<Song>> collectSongs(Artist item) {
        return SongQuery.queryForArtist(
                mContext.getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Artist> items) {
        return SongQuery.queryForArtists(
                mContext.getContentResolver(),
                items)
                .firstOrError();
    }

    @Override
    public Flowable<List<Artist>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> isFavourite(Artist item) {
        return Single.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> changeFavourite(Artist item) {
        return Single.error(new UnsupportedOperationException());
    }
}
