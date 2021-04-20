package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.ArtistRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class ArtistRepositoryImpl extends BaseMediaRepository<Artist> implements ArtistRepository {

    private final static String[] SORT_ORDER_KEYS = {
        ArtistQuery.Sort.BY_ARTIST,
        ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS,
        ArtistQuery.Sort.BY_NUMBER_OF_TRACKS
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, ArtistQuery.Sort.BY_ARTIST);
    }

    public ArtistRepositoryImpl(Context context) {
        super(context);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(ArtistQuery.Sort.BY_ARTIST, R.string.sort_by_name),
            createSortOrder(ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS, R.string.sort_by_number_of_albums),
            createSortOrder(ArtistQuery.Sort.BY_NUMBER_OF_TRACKS, R.string.sort_by_number_of_tracks)
        );
    }

    @Override
    public Flowable<List<Artist>> getAllItems() {
        return ArtistQuery.queryAll(getContext().getContentResolver());
    }

    @Override
    public Flowable<List<Artist>> getAllItems(final String sortOrder) {
        return ArtistQuery.queryAll(
                getContext().getContentResolver(),
                sortOrder);
    }

    @Override
    public Flowable<List<Artist>> getAllItems(String sortOrder, int minSongDuration) {
        return ArtistQuery.queryAll(
                getContext().getContentResolver(),
                sortOrder,
                minSongDuration);
    }

    @Override
    public Flowable<List<Artist>> getFilteredItems(final String filter) {
        return ArtistQuery.queryAllFiltered(
                getContext().getContentResolver(),
                filter);
    }

    @Override
    public Flowable<Artist> getItem(final long id) {
        return ArtistQuery.querySingle(getContext().getContentResolver(), id);
    }

    @Override
    public Completable delete(Artist item) {
        return Del.deleteArtist(getContext(), item);
    }

    @Override
    public Completable delete(Collection<Artist> items) {
        return Del.deleteArtists(getContext(), items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Artist item) {
        return PlaylistHelper.addArtistToPlaylist(
                getContext().getContentResolver(),
                playlistId,
                item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Artist> items) {
        return PlaylistHelper.addItemsToPlaylist(
                getContext().getContentResolver(),
                playlistId,
                items);
    }

    @Override
    public Single<List<Song>> collectSongs(Artist item) {
        return SongQuery.queryForArtist(
                getContext().getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Artist> items) {
        return SongQuery.queryForArtists(
                getContext().getContentResolver(),
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
        return Shortcuts.isShortcutSupported(getContext(), item);
    }

    @Override
    public Completable createShortcut(Artist item) {
        return Shortcuts.createArtistShortcut(getContext(), item);
    }

}
