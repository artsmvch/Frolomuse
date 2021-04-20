package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.GenreRepository;

import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;


public class GenreRepositoryImpl extends BaseMediaRepository<Genre> implements GenreRepository {

    private final static String[] SORT_ORDER_KEYS = {
        GenreQuery.Sort.BY_NAME
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, GenreQuery.Sort.BY_NAME);
    }

    public GenreRepositoryImpl(Context context) {
        super(context);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(GenreQuery.Sort.BY_NAME, R.string.sort_by_name)
        );
    }

    @Override
    public Flowable<List<Genre>> getAllItems() {
        return GenreQuery.queryAll(getContext().getContentResolver());
    }

    @Override
    public Flowable<List<Genre>> getAllItems(final String sortOrder) {
        return GenreQuery.queryAll(getContext().getContentResolver(), sortOrder);
    }

    @Override
    public Flowable<List<Genre>> getAllItems(String sortOrder, int minSongDuration) {
        return GenreQuery.queryAll(getContext().getContentResolver(), sortOrder, minSongDuration);
    }

    @Override
    public Flowable<List<Genre>> getFilteredItems(final String filter) {
        return GenreQuery.queryAllFiltered(getContext().getContentResolver(), filter);
    }

    @Override
    public Flowable<Genre> getItem(final long id) {
        return GenreQuery.querySingle(getContext().getContentResolver(), id);
    }

    @Override
    public Single<Genre> findItemByName(final String name) {
        return GenreQuery.querySingleByName(
                getContext().getContentResolver(),
                name)
                .firstOrError();
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
    public Completable addToPlaylist(long playlistId, Genre item) {
        return PlaylistHelper.addGenreToPlaylist(getContext().getContentResolver(), playlistId, item.getId());
    }

    @Override
    public Completable addToPlaylist(long playlistId, Collection<Genre> items) {
        return PlaylistHelper.addItemsToPlaylist(getContext().getContentResolver(), playlistId, items);
    }

    @Override
    public Single<List<Song>> collectSongs(Genre item) {
        return SongQuery.queryForGenre(
                getContext().getContentResolver(),
                item,
                SongQuery.Sort.BY_TITLE)
                .firstOrError();
    }

    @Override
    public Single<List<Song>> collectSongs(Collection<Genre> items) {
        return SongQuery.queryForGenres(
                getContext().getContentResolver(),
                items)
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
