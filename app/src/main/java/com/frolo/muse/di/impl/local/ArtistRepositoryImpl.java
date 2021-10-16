package com.frolo.muse.di.impl.local;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.ArtistRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public final class ArtistRepositoryImpl extends BaseMediaRepository<Artist> implements ArtistRepository {

    private final static String[] SORT_ORDER_KEYS = {
        ArtistQuery.Sort.BY_ARTIST,
        ArtistQuery.Sort.BY_NUMBER_OF_ALBUMS,
        ArtistQuery.Sort.BY_NUMBER_OF_TRACKS
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, ArtistQuery.Sort.BY_ARTIST);
    }

    public ArtistRepositoryImpl(LibraryConfiguration configuration) {
        super(configuration);
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
        return getSongFilter().switchMap(songFilter -> ArtistQuery.queryAll(getContentResolver(), songFilter));
    }

    @Override
    public Flowable<List<Artist>> getAllItems(final String sortOrder) {
        return getSongFilter().switchMap(songFilter -> ArtistQuery.queryAll(getContentResolver(), songFilter, sortOrder));
    }

    @Override
    public Flowable<List<Artist>> getFilteredItems(final String namePiece) {
        return getSongFilter().switchMap(songFilter -> ArtistQuery.queryAllFiltered(getContentResolver(), songFilter, namePiece));
    }

    @Override
    public Flowable<Artist> getItem(final long id) {
        return ArtistQuery.queryItem(getContext().getContentResolver(), id);
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
    public Completable addToPlaylist(Playlist playlist, Artist item) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addArtistToPlaylist(getContentResolver(), playlist.getId(), item.getId());
        } else {
            // New playlist storage
            return collectSongs(item).flatMapCompletable(songs -> PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getId(), songs));
        }
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Artist> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemsToPlaylist(getContentResolver(), playlist.getId(), items);
        } else {
            // New playlist storage
            return collectSongs(items).flatMapCompletable(songs -> PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getId(), songs));
        }
    }

    @Override
    public Single<List<Song>> collectSongs(Artist item) {
        return getSongFilter().switchMap(songFilter ->
                SongQuery.query(getContentResolver(), songFilter, SongQuery.Sort.BY_ARTIST, item))
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
