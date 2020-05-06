package com.frolo.muse.repository;

import com.frolo.muse.model.media.Artist;

import java.util.List;

import io.reactivex.Flowable;

public interface ArtistRepository extends MediaRepository<Artist> {

    /**
     * Retrieves all artists that have at least one song with a duration not shorter than <code>minSongDuration</code>.
     * The items are sorted by <code>sortOrder</code>
     * @param sortOrder to sort the items
     * @param minSongDuration min acceptable duration for songs in the artists
     * @return artists
     */
    Flowable<List<Artist>> getAllItems(String sortOrder, int minSongDuration);

}
