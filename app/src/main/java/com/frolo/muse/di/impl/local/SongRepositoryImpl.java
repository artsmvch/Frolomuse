package com.frolo.muse.di.impl.local;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.SongFilter;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.SongRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class SongRepositoryImpl extends BaseMediaRepository<Song> implements SongRepository {

    private final static String[] SORT_ORDER_KEYS = {
        SongQuery.Sort.BY_TITLE,
        SongQuery.Sort.BY_ALBUM,
        SongQuery.Sort.BY_ARTIST,
        SongQuery.Sort.BY_DURATION,
        SongQuery.Sort.BY_DATE_ADDED
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, SongQuery.Sort.BY_TITLE);
    }

    public SongRepositoryImpl(LibraryConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(SongQuery.Sort.BY_TITLE, R.string.sort_by_name),
            createSortOrder(SongQuery.Sort.BY_ALBUM, R.string.sort_by_album),
            createSortOrder(SongQuery.Sort.BY_ARTIST, R.string.sort_by_artist),
            createSortOrder(SongQuery.Sort.BY_DURATION, R.string.sort_by_duration),
            createSortOrder(SongQuery.Sort.BY_DATE_ADDED, R.string.sort_by_date_added)
        );
    }

    @Override
    public final Flowable<List<Song>> getAllItems() {
        return SongQuery.queryAll(getContext().getContentResolver());
    }

    @Override
    public final Flowable<List<Song>> getAllItems(final String sortOrder) {
        return getSongFilter().switchMap(filter -> {
            return SongQuery.query(getContext().getContentResolver(), filter, sortOrder);
        });
    }

    @Override
    public final Flowable<List<Song>> getFilteredItems(final String namePiece) {
        return getSongFilter().switchMap(filter -> {
            SongFilter namePiecedFilter = filter.newBuilder().setNamePiece(namePiece).build();
            return SongQuery.query(getContext().getContentResolver(), namePiecedFilter, SongQuery.Sort.BY_TITLE);
        });
    }

    @Override
    public final Flowable<Song> getItem(final long id) {
        return SongQuery.querySingle(getContext().getContentResolver(), id);
    }

    @Override
    public final Completable delete(Song item) {
        return Del.deleteSong(getContext(), item);
    }

    @Override
    public final Completable delete(Collection<Song> items) {
        return Del.deleteSongs(getContext(), items);
    }

    @Override
    public final Completable addToPlaylist(Playlist playlist, Song item) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addSongToPlaylist(
                    getContext().getContentResolver(),
                    playlist.getId(),
                    item.getId());
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext())
                    .addPlaylistMembers(playlist.getId(), Collections.singleton(item));
        }
    }

    @Override
    public final Completable addToPlaylist(Playlist playlist, Collection<Song> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemsToPlaylist(getContext().getContentResolver(), playlist.getId(), items);
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).addPlaylistMembers(playlist.getId(), items);
        }
    }

    @Override
    public final Single<List<Song>> collectSongs(final Song item) {
        return Single.just(Collections.singletonList(item));
    }

    @Override
    public final Single<List<Song>> collectSongs(final Collection<Song> items) {
        return Single.just((List<Song>) new ArrayList<>(items));
    }

    @Override
    public final Flowable<List<Song>> getAllFavouriteItems() {
        return SongQuery.queryAllFavourites(getContext().getContentResolver());
    }

    @Override
    public final Single<Song> getSong(final String path) {
        return SongQuery.querySingleByPath(getContext().getContentResolver(), path).firstOrError();
    }

    @Override
    public final Single<Song> update(
            final Song song,
            final String newTitle,
            final String newAlbum,
            final String newArtist,
            final String newGenre) {

        return SongQuery.update(
                getContext().getContentResolver(),
                song,
                newTitle,
                newAlbum,
                newArtist,
                newGenre)
                .andThen(getItem(song.getId()))
                .firstOrError();
    }

    @Override
    public final Flowable<List<Song>> getSongsFromAlbum(final Album album, final String sortOrder) {
        return getSongFilter().switchMap(filter ->
                SongQuery.query(getContext().getContentResolver(), filter, sortOrder, album));
    }

    @Override
    public final Flowable<List<Song>> getSongsFromArtist(final Artist artist, final String sortOrder) {
        return getSongFilter().switchMap(filter ->
                SongQuery.query(getContext().getContentResolver(), filter, sortOrder, artist));
    }

    @Override
    public final Flowable<List<Song>> getSongsFromGenre(final Genre genre, final String sortOrder) {
        return getSongFilter().switchMap(filter ->
                SongQuery.query(getContext().getContentResolver(), filter, sortOrder, genre));
    }

    @Override
    public final Flowable<List<Song>> getSongsFromPlaylist(final Playlist playlist, final String sortOrder) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return SongQuery.queryForPlaylist(getContext().getContentResolver(), playlist, sortOrder);
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).queryPlaylistMembers(playlist.getId(), sortOrder);
        }
    }

    @Override
    public final Flowable<List<Song>> getRecentlyAddedSongs(final long dateAdded) {
        return getSongFilter().switchMap(filter -> {
            SongFilter recentlyAddedFilter = filter.newBuilder().setTimeAdded(dateAdded).build();
            return SongQuery.query(getContext().getContentResolver(),
                    recentlyAddedFilter, SongQuery.Sort.BY_DATE_ADDED);
        });
    }

    @Override
    public final Flowable<Boolean> isFavourite(final Song item) {
        return SongQuery.isFavourite(getContext().getContentResolver(), item);
    }

    @Override
    public final Completable changeFavourite(final Song item) {
        return SongQuery.changeFavourite(getContext().getContentResolver(), item);
    }

    @Override
    public final Completable addSongPlayCount(Song song, int delta) {
        return SongQuery.addSongPlayCount(getContext().getContentResolver(), song, delta);
    }

    @Override
    public final Single<Boolean> isShortcutSupported(Song item) {
        return Shortcuts.isShortcutSupported(getContext(), item);
    }

    @Override
    public final Completable createShortcut(Song item) {
        return Shortcuts.createSongShortcut(getContext(), item);
    }

}
