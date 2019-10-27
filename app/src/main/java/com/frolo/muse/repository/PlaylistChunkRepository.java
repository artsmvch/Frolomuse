package com.frolo.muse.repository;

import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;

import java.util.Collection;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface PlaylistChunkRepository extends SongRepository {
    /* *******************************************
     * ********************************************
     * ********************************************
     * ********************************************
     * **************   PLAYLIST   ****************
     * ********************************************
     * ********************************************
     * ********************************************
     * ***************************************** */

    Single<Boolean> isSwappingAllowedForSortOrder(String sortOrder);

    Completable addToPlaylist(Playlist playlist, Collection<Song> items);

    Completable removeFromPlaylist(Playlist playlist, Song item);

    Completable swapItemsInPlaylist(Playlist playlist, int fromPos, int toPos);
}
