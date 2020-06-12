package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.PlaylistRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class PlaylistRepositoryImpl extends BaseMediaRepository<Playlist> implements PlaylistRepository {

    private final static String[] SORT_ORDER_KEYS = {
        PlaylistQuery.Sort.BY_NAME,
        PlaylistQuery.Sort.BY_DATE_ADDED,
        PlaylistQuery.Sort.BY_DATE_MODIFIED
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, PlaylistQuery.Sort.BY_NAME);
    }

    public PlaylistRepositoryImpl(final Context context) {
        super(context);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(PlaylistQuery.Sort.BY_NAME, R.string.sort_by_name),
            createSortOrder(PlaylistQuery.Sort.BY_DATE_ADDED, R.string.sort_by_date_added),
            createSortOrder(PlaylistQuery.Sort.BY_DATE_MODIFIED, R.string.sort_by_date_modified)
        );
    }

    @Override
    public Flowable<List<Playlist>> getAllItems() {
        return PlaylistQuery.queryAll(getContext().getContentResolver());
    }

    @Override
    public Flowable<List<Playlist>> getAllItems(final String sortOrder) {
        return PlaylistQuery.queryAll(getContext().getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Playlist>> getFilteredItems(final String filter) {
        return PlaylistQuery.queryAllFiltered(getContext().getContentResolver(), filter);
    }

    @Override
    public Flowable<Playlist> getItem(final long id) {
        return PlaylistQuery.querySingle(getContext().getContentResolver(), id);
    }

    @Override
    public Completable delete(Playlist item) {
        return Del.deletePlaylist(getContext().getContentResolver(), item);
    }

    @Override
    public Completable delete(Collection<Playlist> items) {
        return Del.deletePlaylists(getContext().getContentResolver(), items);
    }

    @Override
    public Completable addToPlaylist(long playlistId, Playlist item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Playlist> items) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<List<Song>> collectSongs(Playlist item) {
        return SongQuery.queryForPlaylist(
                getContext().getContentResolver(),
                item,
                SongQuery.Sort.BY_PLAY_ORDER)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Playlist> items) {
        return SongQuery.queryForPlaylists(
                getContext().getContentResolver(),
                items)
                .firstOrError();
    }

    @Override
    public Single<Playlist> create(final String name) {
        return PlaylistQuery.create(
                getContext(),
                getContext().getContentResolver(),
                name);
    }

    @Override
    public Single<Playlist> update(final Playlist playlist, final String newName) {
        return PlaylistQuery.update(
                getContext(),
                getContext().getContentResolver(),
                playlist,
                newName);
    }

    @Override
    public Flowable<List<Playlist>> getAllFavouriteItems() {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Flowable<Boolean> isFavourite(Playlist item) {
        return Flowable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable changeFavourite(Playlist item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<Boolean> isShortcutSupported(Playlist item) {
        return Shortcuts.isShortcutSupported(getContext(), item);
    }

    @Override
    public Completable createShortcut(Playlist item) {
        return Shortcuts.createPlaylistShortcut(getContext(), item);
    }

}
