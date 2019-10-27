package com.frolo.muse.repository;

import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface AlbumRepository extends MediaRepository<Album> {

    /**
     * Retrieves albums for the given artist
     * @param artist artist
     * @return albums of artist
     */
    Flowable<List<Album>> getAlbumsOfArtist(Artist artist);

    Completable updateArt(long albumId, String artFilePath);
}
