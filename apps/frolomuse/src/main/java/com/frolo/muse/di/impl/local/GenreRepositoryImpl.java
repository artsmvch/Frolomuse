package com.frolo.muse.di.impl.local;

import com.frolo.muse.R;
import com.frolo.music.model.Genre;
import com.frolo.music.model.Playlist;
import com.frolo.music.model.Song;
import com.frolo.music.model.SortOrder;
import com.frolo.music.repository.GenreRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public final class GenreRepositoryImpl extends BaseMediaRepository<Genre> implements GenreRepository {

    private final static String[] SORT_ORDER_KEYS = {
        GenreQuery.Sort.BY_NAME
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, GenreQuery.Sort.BY_NAME);
    }

    public GenreRepositoryImpl(LibraryConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(GenreQuery.Sort.BY_NAME, R.string.sort_by_name)
        );
    }

    @Override
    public Flowable<List<Genre>> getAllItems() {
        return getSongFilter().switchMap(songFilter -> GenreQuery.queryAll(getContentResolver(), songFilter));
    }

    @Override
    public Flowable<List<Genre>> getAllItems(final String sortOrder) {
        return getSongFilter().switchMap(songFilter -> GenreQuery.queryAll(getContentResolver(), songFilter, sortOrder));
    }

    @Override
    public Flowable<List<Genre>> getFilteredItems(final String namePiece) {
        return getSongFilter().switchMap(songFilter -> GenreQuery.queryAllFiltered(getContentResolver(), songFilter, namePiece));
    }

    @Override
    public Flowable<Genre> getItem(final long id) {
        return GenreQuery.queryItem(getContentResolver(), id);
    }

    @Override
    public Single<Genre> findItemByName(final String name) {
        return GenreQuery.queryItemByName(getContentResolver(), name).firstOrError();
    }

    @Override
    public Completable delete(Genre item) {
        return Del.deleteGenre(getContext(), item);
    }

    @Override
    public Completable delete(Collection<Genre> items) {
        return Del.deleteGenres(getContext(), items);
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Genre item) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addGenreToPlaylist(getContext().getContentResolver(), playlist.getMediaId().getSourceId(), item.getMediaId().getSourceId());
        } else {
            // New playlist storage
            return collectSongs(item).flatMapCompletable(songs -> PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getMediaId().getSourceId(), songs));
        }
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Genre> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemsToPlaylist(getContext().getContentResolver(), playlist.getMediaId().getSourceId(), items);
        } else {
            // New playlist storage
            return collectSongs(items).flatMapCompletable(songs -> PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getMediaId().getSourceId(), songs));
        }
    }

    @Override
    public Single<List<Song>> collectSongs(Genre item) {
        return getSongFilter().switchMap(songFilter ->
                SongQuery.query(getContentResolver(), songFilter, SongQuery.Sort.BY_TITLE, item))
                .firstOrError();
    }

    @Override
    public Flowable<List<Genre>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<Boolean> isFavourite(Genre item) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable changeFavourite(Genre item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> isShortcutSupported(Genre item) {
        return Shortcuts.isShortcutSupported(getContext(), item);
    }

    @Override
    public Completable createShortcut(Genre item) {
        return Shortcuts.createGenreShortcut(getContext(), item);
    }

}
