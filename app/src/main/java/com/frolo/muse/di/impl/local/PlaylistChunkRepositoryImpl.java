package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.sort.SortOrder;
import com.frolo.muse.repository.PlaylistChunkRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;


public class PlaylistChunkRepositoryImpl extends SongRepositoryImpl implements PlaylistChunkRepository {

    private final static String[] SORT_ORDER_KEYS = {
        SongQuery.Sort.BY_PLAY_ORDER,
        SongQuery.Sort.BY_TITLE,
        SongQuery.Sort.BY_ALBUM,
        SongQuery.Sort.BY_ARTIST,
        SongQuery.Sort.BY_DURATION,
        SongQuery.Sort.BY_DATE_ADDED
    };

    static String getSortOrderOrDefault(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(candidate, SORT_ORDER_KEYS, SongQuery.Sort.BY_PLAY_ORDER);
    }

    public PlaylistChunkRepositoryImpl(Context context) {
        super(context);
    }

    @Override
    protected List<SortOrder> blockingGetSortOrders() {
        return collectSortOrders(
            createSortOrder(SongQuery.Sort.BY_PLAY_ORDER, R.string.sort_by_play_order),
            createSortOrder(SongQuery.Sort.BY_TITLE, R.string.sort_by_name),
            createSortOrder(SongQuery.Sort.BY_ALBUM, R.string.sort_by_album),
            createSortOrder(SongQuery.Sort.BY_ARTIST, R.string.sort_by_artist),
            createSortOrder(SongQuery.Sort.BY_DURATION, R.string.sort_by_duration),
            createSortOrder(SongQuery.Sort.BY_DATE_ADDED, R.string.sort_by_date_added)
        );
    }

    @Override
    public Single<Boolean> isMovingAllowedForSortOrder(String sortOrder) {
        return Single.just(sortOrder.equals(SongQuery.Sort.BY_PLAY_ORDER));
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Song> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.addItemsToPlaylist(getContext().getContentResolver(), playlist.getId(), items);
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext()).addPlaylistMembers(playlist.getId(), items);
        }
    }

    @Override
    public Completable removeFromPlaylist(Playlist playlist, Song item) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.removeFromPlaylist(
                    getContext().getContentResolver(),
                    playlist.getId(),
                    item);
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext())
                    .removePlaylistMembers(playlist.getId(), Collections.singleton(item));
        }
    }

    @Override
    public Completable removeFromPlaylist(Playlist playlist, Collection<Song> items) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.removeFromPlaylist(
                    getContext().getContentResolver(),
                    playlist.getId(),
                    items);
        } else {
            // New playlist storage
            return PlaylistDatabaseManager.get(getContext())
                    .removePlaylistMembers(playlist.getId(), items);
        }
    }

    @Override
    public Completable moveItemInPlaylist(Playlist playlist, int fromPos, int toPos) {
        if (playlist.isFromSharedStorage()) {
            // Legacy
            return PlaylistHelper.moveItemInPlaylist(getContext().getContentResolver(), playlist.getId(), fromPos, toPos);
        } else {
            return Completable.error(new UnsupportedOperationException(
                    "Use moveItemInPlaylist(MoveOp) method for playlists from application storage"));
        }
    }

    @Override
    public Completable moveItemInPlaylist(MoveOp op) {
        return PlaylistDatabaseManager.get(getContext()).movePlaylistMember(op.target, op.previous, op.next);
    }
}
