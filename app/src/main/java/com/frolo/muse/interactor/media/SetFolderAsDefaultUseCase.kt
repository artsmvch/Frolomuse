package com.frolo.muse.interactor.media

import com.frolo.muse.model.media.MyFile
import com.frolo.muse.repository.MyFileRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject


class SetFolderAsDefaultUseCase @Inject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val myFileRepository: MyFileRepository
) {

    fun setFolderAsDefault(folder: MyFile): Completable {
        return myFileRepository.setDefaultFolder(folder)
                .subscribeOn(schedulerProvider.worker())
    }

}