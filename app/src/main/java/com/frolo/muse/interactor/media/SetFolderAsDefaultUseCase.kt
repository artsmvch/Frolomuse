package com.frolo.muse.interactor.media

import com.frolo.music.model.MyFile
import com.frolo.music.repository.MyFileRepository
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