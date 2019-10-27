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

    private final Map<String, String> mSortOrders;

    public PlaylistChunkRepositoryImpl(Context context) {
        super(context);
        mSortOrders = new LinkedHashMap<String, String>(4, 1f) {{
            put(SongQuery.Sort.BY_PLAY_ORDER,
                    getContext().getString(R.string.sort_by_play_order));

            put(SongQuery.Sort.BY_TITLE,
                    getContext().getString(R.string.sort_by_name));

            put(SongQuery.Sort.BY_ALBUM,
                    getContext().getString(R.string.sort_by_album));

            put(SongQuery.Sort.BY_ARTIST,
                    getContext().getString(R.string.sort_by_artist));
        }};
    }

    @Override
    public Single<Map<String, String>> getSortOrders() {
        return Single.just(mSortOrders);
    }

    @Override
    public Single<Boolean> isSwappingAllowedForSortOrder(String sortOrder) {
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
    public Completable swapItemsInPlaylist(Playlist playlist, int fromPos, int toPos) {
        return PlaylistHelper.swapItemsInPlaylist(
                getContext().getContentResolver(),
                playlist.getId(),
                fromPos,
                toPos);
    }
}
