package com.frolo.muse.repository;

import com.frolo.muse.model.media.Genre;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public interface GenreRepository extends MediaRepository<Genre> {

    /**
     * Retrieves all genres that have at least one song with a duration not shorter than <code>minSongDuration</code>.
     * The items are sorted by <code>sortOrder</code>
     * @param sortOrder to sort the items
     * @param minSongDuration min acceptable duration for songs in the genres
     * @return genres
     */
    Flowable<List<Genre>> getAllItems(String sortOrder, int minSongDuration);

    Single<Genre> findItemByName(String name);

}
