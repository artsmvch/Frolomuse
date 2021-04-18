package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.repository.MediaFileRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import javax.inject.Inject


class GetAudioBucketsUseCase @Inject constructor(
    private val repository: MediaFileRepository,
    private val schedulerProvider: SchedulerProvider
) {

    fun getBuckets(): Flowable<List<MediaBucket>> {
        return repository.getAudioFiles()
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.computation())
            .map { audioFiles ->
                audioFiles.map { audioFile ->
                    MediaBucket(
                        id = audioFile.bucketId,
                        displayName = audioFile.bucketName.orEmpty()
                    )
                }.distinctBy { bucket -> bucket.id }
            }
    }

}