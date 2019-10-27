package com.frolo.muse.repository;

import com.frolo.muse.model.media.Genre;

import io.reactivex.Single;

public interface GenreRepository extends MediaRepository<Genre> {

    Single<Genre> findItemByName(String name);

}
