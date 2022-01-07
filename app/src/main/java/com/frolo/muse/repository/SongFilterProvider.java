package com.frolo.muse.repository;

import com.frolo.music.model.SongFilter;

import io.reactivex.Flowable;


public interface SongFilterProvider {
    Flowable<SongFilter> getSongFilter();
}
