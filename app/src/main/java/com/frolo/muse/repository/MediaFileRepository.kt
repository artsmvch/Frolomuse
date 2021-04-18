package com.frolo.muse.repository

import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.model.media.MediaFile
import io.reactivex.Flowable


interface MediaFileRepository: MediaRepository<MediaFile> {
    fun getAudioFiles(): Flowable<List<MediaFile>>
    fun getSortedAudioFiles(bucket: MediaBucket, sortOrder: String?): Flowable<List<MediaFile>>
}