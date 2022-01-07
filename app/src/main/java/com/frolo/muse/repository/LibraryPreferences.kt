package com.frolo.muse.repository

import com.frolo.music.model.SongFilter
import com.frolo.music.model.SongType
import com.frolo.music.repository.SongFilterProvider
import io.reactivex.Completable
import io.reactivex.Flowable


interface LibraryPreferences : SongFilterProvider {
    override fun getSongFilter(): Flowable<SongFilter>

    fun getSongTypes(): Flowable<List<SongType>>
    fun setSongTypes(types: Collection<SongType>): Completable

    fun getMinAudioDuration(): Flowable<Long>
    fun setMinAudioDuration(duration: Long): Completable
}