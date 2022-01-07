package com.frolo.muse.repository;

import com.frolo.music.model.Album;
import com.frolo.music.model.Artist;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface AlbumRepository extends MediaRepository<Album> {

    /**
     * Returns an item for preview (for example, theme preview).
     * @return item for preview
     */
    Flowable<Album> getItemForPreview();

    /**
     * Retrieves albums for the given artist
     * @param artist artist
     * @return albums of artist
     */
    Flowable<List<Album>> getAlbumsOfArtist(Artist artist);

    Completable updateArt(long albumId, String artFilePath);
}
