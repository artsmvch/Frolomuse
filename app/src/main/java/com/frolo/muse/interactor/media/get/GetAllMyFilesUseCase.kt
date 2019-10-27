package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.muse.model.media.MyFile
import com.frolo.muse.repository.MyFileRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import javax.inject.Inject


class GetAllMyFilesUseCase @Inject constructor(
        schedulerProvider: SchedulerProvider,
        private val repository: MyFileRepository,
        private val preferences: Preferences
): GetSectionedMediaUseCase<MyFile>(
        Library.FOLDERS,
        schedulerProvider,
        repository,
        preferences
) {

    data class GoBackResult constructor(
            val canGoBack: Boolean,
            val toBrowse: MyFile?
    )

    private val rootProcessor: BehaviorProcessor<MyFile> by lazy {
        BehaviorProcessor.createDefault<MyFile>(
                repository.rootFile.blockingGet())
    }

    private fun browse(
            myFile: MyFile,
            checkForReversion: Boolean
    ): Flowable<List<MyFile>> {
        rootProcessor.onNext(myFile)
        return repository.browse(myFile)
                .map { list ->
                    if (checkForReversion
                            && preferences.isSortOrderReversedForSection(Library.FOLDERS)) {
                        list.reversed()
                    } else list
                }
    }

    fun getRoot(): Flowable<MyFile> {
        return rootProcessor
    }

    override fun getSortedCollection(sortOrder: String): Flowable<List<MyFile>> {
        val currRoot = rootProcessor.value!!
        // do not check for the reversion here, the caller of this method will do it itself
        return browse(currRoot, false)
    }

    // Tries to find cached file list for the given [myFile].
    // If not found, then force the repository force the given [myFile].
    fun browse(myFile: MyFile): Flowable<List<MyFile>> {
        return browse(myFile, true)
    }

    fun goBack(): GoBackResult {
        val currentRoot = rootProcessor.value
                ?: return GoBackResult(false, null)

        if (currentRoot == repository.rootFile.blockingGet()) {
            // Theoretically we cannot browse the parent of the root
            return GoBackResult(false, null)
        }

        // lifting to the parent of the current root if possible
        val parentRoot = currentRoot.parent
                ?: return GoBackResult(false, null)

        return GoBackResult(true, parentRoot)
    }

}