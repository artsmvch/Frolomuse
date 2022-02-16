package com.frolo.muse.ui.main.audiofx.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ComponentInjector
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.repository.PresetRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class SavePresetVMFactory constructor(
    injector: ComponentInjector,
    private val bandLevels: ShortArray
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var repository: PresetRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SavePresetViewModel(
            schedulerProvider,
            repository,
            eventLogger,
            bandLevels
        ) as T
    }

}