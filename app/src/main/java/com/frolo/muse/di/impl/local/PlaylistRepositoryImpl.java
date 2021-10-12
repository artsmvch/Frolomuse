package com.frolo.muse.di.impl.local;

import com.frolo.muse.Features;
import com.frolo.muse.R;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.PlaylistRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class PlaylistRepositoryImpl extends BaseMediaRepository<Playlist> implements PlaylistRepository {

    private final static String[] SORT_ORDER_KEYS = {
        PlaylistQuery.Sort.BY_NAME,
        PlaylistQuery.Sort.BY_DATE_ADDED,
        PlaylistQuery.Sort.BY_DATE_MODIFIED
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, PlaylistQuery.Sort.BY_NAME);
    }

    public PlaylistRepositoryImpl(LibraryConfiguration configuration) {
        super(configuration);
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
    public Completable transferFromSharedStorage() {
        if (!Features.isAppPlaylistStorageFeatureAvailable()) {
            // Transfer is not allowed if the App-Playlist-Storage feature is not available
            return Completable.complete();
        }

        final Scheduler workerScheduler = Schedulers.io();
        return PlaylistQuery.queryAll(getContext().getContentResolver(), null)
            .subscribeOn(workerScheduler)
            .first(Collections.emptyList())
            .flatMap((Function<List<Playlist>, SingleSource<List<PlaylistTransfer.Op>>>) playlists -> {
                if (playlists.isEmpty()) {
                    // No playlists to transfer
                    return Single.just(Collections.emptyList());
                }

                // Collecting all op sources and zipping them
                List<Single<PlaylistTransfer.Op>> sources = new ArrayList<>(playlists.size());
                for (Playlist playlist : playlists) {
                    Single<PlaylistTransfer.Op> source = collectSongs(playlist)
                        .subscribeOn(workerScheduler)
                        .map(songs -> new PlaylistTransfer.Op(playlist, songs));
                    sources.add(source);
                }
                Function<Object[], List<PlaylistTransfer.Op>> zipper = objects -> {
                    List<PlaylistTransfer.Op> resultList = new ArrayList<>(objects.length);
                    for (Object obj : objects) {
                        resultList.add((PlaylistTransfer.Op) obj);
                    }
                    return resultList;
                };
                return Single.zip(sources, zipper);
            })
            .flatMap(playlistCreationOps ->
                // Transferring playlists in the app database
                PlaylistDatabaseManager
                    .get(getContext())
                    .transferPlaylists(playlistCreationOps)
            )
            .flatMapCompletable(results ->
                // Transferring shortcuts
                Shortcuts.transferPlaylistShortcuts(getContext(), results)
                    // Ignoring any error, cause it is not that important
                    .onErrorComplete()
            );
    }

    @Override
    public Flowable<List<Playlist>> getAllItems() {
        return getAllItems(PlaylistQuery.Sort.BY_NAME);
    }

    @Override
    public Flowable<List<Playlist>> getAllItems(final String sortOrder) {
        if (Features.isAppPlaylistStorageFeatureAvailable()) {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).queryAllPlaylists(sortOrder);
        } else {
            // Legacy
            return PlaylistQuery.queryAll(getContext().getContentResolver(), sortOrder);
        }
    }

    @Override
    public Flowable<List<Playlist>> getFilteredItems(final String namePiece) {
        if (Features.isAppPlaylistStorageFeatureAvailable()) {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).queryAllPlaylistsFiltered(namePiece);
        } else {
            // Legacy
            return PlaylistQuery.queryAllFiltered(getContext().getContentResolver(), namePiece);
        }
    }

    @Override
    public Flowable<Playlist> getItem(Playlist item) {
        if (item.isFromSharedStorage()) {
            // Legacy
            return PlaylistQuery.querySingle(getContext().getContentResolver(), item.getId());
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).queryPlaylist(item.getId());
        }
    }

    @Deprecated
    @Override
    public Flowable<Playlist> getItem(final long id) {
        Flowable<Playlist> fallback =
                PlaylistQuery.querySingle(getContext().getContentResolver(), id);
        if (Features.isAppPlaylistStorageFeatureAvailable()) {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext())
                .queryPlaylist(id)
                .onErrorResumeNext(fallback);
        } else {
            // Legacy
            return fallback;
        }
    }

    @Override
    public Completable delete(Playlist item) {
        if (item.isFromSharedStorage()) {
            // Legacy
            return Del.deletePlaylist(getContext(), item);
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).deletePlaylist(item);
        }
    }

    @Override
    public Completable delete(Collection<Playlist> items) {
        // First, splitting the collection of playlist items into two: playlists from
        // the shared storage and playlists from the application storage.
        // Each chunk of playlists is deleted in its own way.
        Collection<Playlist> playlistsFromSharedStorage = new LinkedList<>();
        Collection<Playlist> playlistsFromApplicationStorage = new LinkedList<>();
        for (Playlist item : items) {
            if (item.isFromSharedStorage()) {
                playlistsFromSharedStorage.add(item);
            } else {
                playlistsFromApplicationStorage.add(item);
            }
        }

        // Legacy
        Completable source1 = Del.deletePlaylists(getContext(), playlistsFromSharedStorage);

        // New playlist storage
        Completable source2 = PlaylistDatabaseManager
                .get(getContext())
                .deletePlaylists(playlistsFromApplicationStorage);

        return Completable.merge(Arrays.asList(source1, source2));
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Playlist item) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Playlist> items) {
        return Completable.error(new UnsupportedOperationException());
    }

    @Override
    public Single<List<Song>> collectSongs(Playlist item) {
        if (item.isFromSharedStorage()) {
            // Legacy
            return SongQuery.queryForPlaylist(
                    getContext().getContentResolver(),
                    item,
                    SongQuery.Sort.BY_PLAY_ORDER)
                    .firstOrError();
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext())
                    .queryPlaylistMembers(item.getId())
                    .firstOrError();
        }
    }

    @Override
    public Single<List<Song>> collectSongs(final Collection<Playlist> items) {
        return Single.defer((Callable<Single<List<Song>>>) () -> {
            // Collecting single sources
            List<Single<List<Song>>> sources = new ArrayList<>();
            for (Playlist playlist : items) {
                Single<List<Song>> source = collectSongs(playlist);
                sources.add(source);
            }

            Function<Object[], List<Song>> zipper = objects -> {
                List<Song> resultList = new ArrayList<>();
                for (Object obj : objects) {
                    //noinspection unchecked
                    resultList.addAll((List<Song>) obj);
                }
                return resultList;
            };
            // Zipping single sources
            return Single.zip(sources, zipper);
        });
    }

    @Override
    public Single<Playlist> create(final String name) {
        if (Features.isAppPlaylistStorageFeatureAvailable()) {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).createPlaylist(name);
        } else {
            // Legacy
            return PlaylistQuery.create(
                    getContext(),
                    getContext().getContentResolver(),
                    name);
        }
    }

    @Override
    public Single<Playlist> update(final Playlist playlist, final String newName) {
        final Single<Playlist> updateSource;
        if (playlist.isFromSharedStorage()) {
            // Legacy
            updateSource = PlaylistQuery.update(
                    getContext(), getContext().getContentResolver(), playlist, newName);
        } else {
            // New playlist storage
            updateSource = PlaylistDatabaseManager.get(getContext()).updatePlaylist(playlist, newName);
        }

        return updateSource
            .flatMap(updated ->
                Shortcuts.updateMediaShortcut(getContext(), updated)
                    .onErrorComplete()
                    .andThen(Single.just(updated))
            );
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
