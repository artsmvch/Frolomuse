package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.AlbumRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class AlbumRepositoryImpl extends BaseMediaRepository<Album> implements AlbumRepository {

    private final static String[] SORT_ORDER_KEYS = {
        AlbumQuery.Sort.BY_ALBUM,
        AlbumQuery.Sort.BY_NUMBER_OF_SONGS
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, AlbumQuery.Sort.BY_ALBUM);
    }

    public AlbumRepositoryImpl(Context context) {
        super(context);
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
        return AlbumQuery.queryAll(getContext().getContentResolver());
    }

    @Override
    public Flowable<List<Album>> getAllItems(final String sortOrder) {
        return AlbumQuery.queryAll(getContext().getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Album>> getAllItems(String sortOrder, int minSongDuration) {
        return AlbumQuery.queryAll(getContext().getContentResolver(), sortOrder, minSongDuration);
    }

    @Override
    public Flowable<List<Album>> getFilteredItems(final String filter) {
        return AlbumQuery.queryAllFiltered(getContext().getContentResolver(), filter);
    }

    @Override
    public Flowable<Album> getItem(final long id) {
        return AlbumQuery.querySingle(getContext().getContentResolver(), id);
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
    public Completable addToPlaylist(long playlistId, Album item) {
        return PlaylistHelper.addAlbumToPlaylist(
                getContext().getContentResolver(),
                playlistId,
                item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Album> items) {
        return PlaylistHelper.addItemsToPlaylist(
                getContext().getContentResolver(),
                playlistId,
                items);
    }

    @Override
    public Single<List<Song>> collectSongs(Album item) {
        return SongQuery.queryForAlbum(
                getContext().getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Album> items) {
        return SongQuery.queryForAlbums(
                getContext().getContentResolver(),
                items)
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
        return AlbumQuery.queryForArtist(
                getContext().getContentResolver(),
                artist.getId());
    }

    @Override
    public Completable updateArt(final long albumId, final String filepath) {
        return AlbumQuery.updateAlbumArtPath(
                getContext().getContentResolver(),
                albumId,
                filepath);
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
