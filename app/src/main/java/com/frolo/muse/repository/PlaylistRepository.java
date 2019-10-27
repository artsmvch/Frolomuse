package com.frolo.muse.repository;


import com.frolo.muse.model.media.Playlist;

import io.reactivex.Single;

public interface PlaylistRepository extends MediaRepository<Playlist> {
    /**
     * Creates a playlist with given name;
     * @param title of the new playlist
     * @return newly created playlist object
     */
    Single<Playlist> create(String title);

    Single<Playlist> update(Playlist playlist, String newName);
}
