package com.frolo.music.repository;

import com.frolo.music.model.Genre;

import io.reactivex.Single;

public interface GenreRepository extends MediaRepository<Genre> {

    Single<Genre> findItemByName(String name);

}
