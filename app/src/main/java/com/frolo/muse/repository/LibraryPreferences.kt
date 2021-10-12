package com.frolo.muse.repository

import com.frolo.muse.model.media.SongFilter
import io.reactivex.Completable
import io.reactivex.Flowable


interface LibraryPreferences : SongFilterProvider {
    override fun getSongFilter(): Flowable<SongFilter>
    fun setSongFilter(filter: SongFilter): Completable
}