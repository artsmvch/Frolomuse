package com.frolo.muse.repository;

import com.frolo.muse.model.media.SongFilter;

import io.reactivex.Flowable;


public interface SongFilterProvider {
    Flowable<SongFilter> getSongFilter();
}
