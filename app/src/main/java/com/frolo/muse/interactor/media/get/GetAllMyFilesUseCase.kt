package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.Library
import com.frolo.music.model.MyFile
import com.frolo.muse.repository.MyFileRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import javax.inject.Inject


@Deprecated(
    message = "MyFile is replaced with MediaFile",
    replaceWith = ReplaceWith("GetMediaFileTreeUseCase")
)
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

    data class GoBackResult constructor(val canGoBack: Boolean, val toBrowse: MyFile?)

    private val rootProcessor: BehaviorProcessor<MyFile> by lazy {
        BehaviorProcessor.createDefault<MyFile>(repository.defaultFolder.blockingGet())
    }

    private fun browse(myFile: MyFile, checkForReversion: Boolean): Flowable<List<MyFile>> {
        rootProcessor.onNext(myFile)

        val section = Library.FOLDERS

        return preferences.getSortOrderForSection(section)
            .switchMap { sortOrderKey ->
                val source = repository.browse(myFile, sortOrderKey)

                if (!checkForReversion) return@switchMap source

                return@switchMap source.switchMap { list ->
                    preferences.isSortOrderReversedForSection(section).map { reversed ->
                        if (reversed) list.reversed() else list
                    }
                }
            }
    }

    fun getRoot(): Flowable<MyFile> {
        return rootProcessor
    }

    override fun getSortedCollection(sortOrder: String): Flowable<List<MyFile>> {
        val currRoot = rootProcessor.value
        // do not check for the reversion here, the caller of this method will do it itself
        return Single.fromCallable { requireNotNull(currRoot) }
                .flatMapPublisher { browse(it, false) }
    }

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