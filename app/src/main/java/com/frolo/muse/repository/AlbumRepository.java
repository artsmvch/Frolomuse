package com.frolo.muse.repository;

import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface AlbumRepository extends MediaRepository<Album> {

    /**
     * Retrieves all albums that have at least one song with a duration not shorter than <code>minSongDuration</code>.
     * The items are sorted by <code>sortOrder</code>
     * @param sortOrder to sort the items
     * @param minSongDuration min acceptable duration for songs in the albums
     * @return albums
     */
    Flowable<List<Album>> getAllItems(String sortOrder, int minSongDuration);

    /**
     * Retrieves albums for the given artist
     * @param artist artist
     * @return albums of artist
     */
    Flowable<List<Album>> getAlbumsOfArtist(Artist artist);

    Completable updateArt(long albumId, String artFilePath);
}
