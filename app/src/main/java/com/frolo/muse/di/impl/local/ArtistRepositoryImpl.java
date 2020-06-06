package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.ArtistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class ArtistRepositoryImpl implements ArtistRepository {

    private final static String[] SORT_ORDER_KEYS = {
        ArtistQuery.Sort.BY_ARTIST,
        ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS,
        ArtistQuery.Sort.BY_NUMBER_OF_TRACKS
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, ArtistQuery.Sort.BY_ARTIST);
    }

    private final Context mContext;
    private final List<SortOrder> mSortOrders;

    public ArtistRepositoryImpl(final Context context) {
        this.mContext = context;
        this.mSortOrders = new ArrayList<SortOrder>(3) {{
            add(new SortOrderImpl(mContext, ArtistQuery.Sort.BY_ARTIST, R.string.sort_by_name));
            add(new SortOrderImpl(mContext, ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS, R.string.sort_by_number_of_albums));
            add(new SortOrderImpl(mContext, ArtistQuery.Sort.BY_NUMBER_OF_TRACKS, R.string.sort_by_number_of_tracks));
        }};
    }

    @Override
    public Single<List<SortOrder>> getSortOrders() {
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
    public Flowable<List<Artist>> getAllItems(String sortOrder, int minSongDuration) {
        return ArtistQuery.queryAll(
                mContext.getContentResolver(),
                sortOrder,
                minSongDuration);
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
    public Flowable<Boolean> isFavourite(Artist item) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable changeFavourite(Artist item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> isShortcutSupported(Artist item) {
        return Shortcuts.isShortcutSupported(mContext, item);
    }

    @Override
    public Completable createShortcut(Artist item) {
        return Shortcuts.createArtistShortcut(mContext, item);
    }

}
