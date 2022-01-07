package com.frolo.music.repository

import com.frolo.music.model.MediaBucket
import com.frolo.music.model.MediaFile
import io.reactivex.Flowable


interface MediaFileRepository: MediaRepository<MediaFile> {
    fun getAudioFiles(): Flowable<List<MediaFile>>
    fun getSortedAudioFiles(bucket: MediaBucket, sortOrder: String?): Flowable<List<MediaFile>>
    fun getAudioFiles(bucket: MediaBucket): Flowable<List<MediaFile>>
}