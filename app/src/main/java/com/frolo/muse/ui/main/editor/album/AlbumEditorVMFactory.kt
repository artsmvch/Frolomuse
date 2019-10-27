package com.frolo.muse.ui.main.editor.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.repository.AlbumRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AlbumEditorVMFactory constructor(
        appComponent: AppComponent,
        private val album: Album
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var repository: AlbumRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlbumEditorViewModel(
                schedulerProvider,
                repository,
                eventLogger,
                album
        ) as T
    }

}