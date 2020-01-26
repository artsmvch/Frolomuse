package com.frolo.muse.di.impl.local;

import android.content.Context;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.PlaylistChunkRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;


public class PlaylistChunkRepositoryImpl
        extends SongRepositoryImpl
        implements PlaylistChunkRepository {

    private final static String[] SORT_ORDER_KEYS = {
            SongQuery.Sort.BY_PLAY_ORDER,
            SongQuery.Sort.BY_TITLE,
            SongQuery.Sort.BY_ALBUM,
            SongQuery.Sort.BY_ARTIST,
            SongQuery.Sort.BY_DURATION
    };

    // Returns sort order candidate if valid or default
    static String validateSortOrder(String candidate) {
        return Preconditions.takeIfNotNullAndListedOrDefault(
                candidate,
                SORT_ORDER_KEYS,
                SongQuery.Sort.BY_PLAY_ORDER);
    }

    private final Map<String, String> mSortOrders;

    public PlaylistChunkRepositoryImpl(Context context) {
        super(context);
        mSortOrders = new LinkedHashMap<String, String>(5, 1f) {{
            put(SongQuery.Sort.BY_PLAY_ORDER,
                    getContext().getString(R.string.sort_by_play_order));

            put(SongQuery.Sort.BY_TITLE,
                    getContext().getString(R.string.sort_by_name));

            put(SongQuery.Sort.BY_ALBUM,
                    getContext().getString(R.string.sort_by_album));

            put(SongQuery.Sort.BY_ARTIST,
                    getContext().getString(R.string.sort_by_artist));

            put(SongQuery.Sort.BY_DURATION,
                    getContext().getString(R.string.sort_by_duration));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Single<Boolean> isMovingAllowedForSortOrder(String sortOrder) {
        return Single.just(sortOrder.equals(SongQuery.Sort.BY_PLAY_ORDER));
    }

    @Override
    public Completable addToPlaylist(Playlist playlist, Collection<Song> items) {
        return PlaylistHelper.addItemsToPlaylist(
                getContext().getContentResolver(),
                playlist.getId(),
                items);
    }

    @Override
    public Completable removeFromPlaylist(Playlist playlist, Song item) {
        return PlaylistHelper.removeFromPlaylist(
                getContext().getContentResolver(),
                playlist.getId(),
                item);
    }

    @Override
    public Completable moveItemInPlaylist(Playlist playlist, int fromPos, int toPos) {
        return PlaylistHelper.moveItemInPlaylist(
                getContext().getContentResolver(),
                playlist.getId(),
                fromPos,
                toPos);
    }
}
