package com.frolo.muse.di.impl.local;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.AlbumRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public final class AlbumRepositoryImpl extends BaseMediaRepository<Album> implements AlbumRepository {

    private final static String[] SORT_ORDER_KEYS = {
        AlbumQuery.Sort.BY_ALBUM,
        AlbumQuery.Sort.BY_NUMBER_OF_SONGS
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, AlbumQuery.Sort.BY_ALBUM);
    }

    public AlbumRepositoryImpl(LibraryConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(AlbumQuery.Sort.BY_ALBUM, R.string.sort_by_name),
            createSortOrder(AlbumQuery.Sort.BY_NUMBER_OF_SONGS, R.string.sort_by_number_of_songs)
        );
    }

    @Override
    public Flowable<List<Album>> getAllItems() {
        return getSongFilter().switchMap(filter ->
                AlbumQuery.queryAll(getContext().getContentResolver(), filter, AlbumQuery.Sort.BY_ALBUM));
    }

    @Override
    public Flowable<List<Album>> getAllItems(final String sortOrder) {
        return getSongFilter().switchMap(filter ->
                AlbumQuery.queryAll(getContext().getContentResolver(), filter, sortOrder));
    }

    @Override
    public Flowable<Album> getItemForPreview() {
        return AlbumQuery.queryForPreview(getContext().getContentResolver());
    }

    @Override
    public Flowable<List<Album>> getFilteredItems(final String namePiece) {
        return getSongFilter().switchMap(filter ->
                AlbumQuery.queryAllFiltered(getContext().getContentResolver(), filter, namePiece));
    }

    @Override
    public Flowable<Album> getItem(final long id) {
        return AlbumQuery.queryItem(getContext().getContentResolver(), id);
    }

    @Override
    public Completable delete(Album item) {
        return Del.deleteAlbum(getContext(), item);
    }

    @Override
    public Completable delete(Collection<Album> items) {
        return Del.deleteAlbums(getContext(), items);
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Album item) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addAlbumToPlaylist(
                    getContext().getContentResolver(),
                    playlist.getId(),
                    item.getId());
        } else {
            // New playlist storage
            return collectSongs(item).flatMapCompletable(songs -> PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getId(), songs));
        }
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Album> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemsToPlaylist(
                    getContext().getContentResolver(),
                    playlist.getId(),
                    items);
        } else {
            // New playlist storage
            return collectSongs(items).flatMapCompletable(songs -> PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getId(), songs));
        }
    }

    @Override
    public Single<List<Song>> collectSongs(Album item) {
        return getSongFilter().switchMap(songFilter ->
                SongQuery.query(getContentResolver(), songFilter, AlbumQuery.Sort.BY_ALBUM, item))
                .firstOrError();
    }

    @Override
    public Flowable<List<Album>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<Boolean> isFavourite(Album item) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable changeFavourite(Album item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<List<Album>> getAlbumsOfArtist(final Artist artist) {
        return getSongFilter().switchMap(filter ->
                AlbumQuery.queryForArtist(getContext().getContentResolver(), filter, artist.getId()));
    }

    @Override
    public Completable updateArt(final long albumId, final String filepath) {
        return AlbumQuery.updateAlbumArtPath(getContext().getContentResolver(), albumId, filepath);
    }

    @Override
    public Single<Boolean> isShortcutSupported(Album item) {
        return Shortcuts.isShortcutSupported(getContext(), item);
    }

    @Override
    public Completable createShortcut(Album item) {
        return Shortcuts.createAlbumShortcut(getContext(), item);
    }

}
