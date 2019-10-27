package com.frolo.muse.ui.main.player.poster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.App
import com.frolo.muse.di.AppComponent
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class PosterVMFactory constructor(
        private val app: App,
        appComponent: AppComponent,
        private val song: Song
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var navigator: Navigator
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PosterViewModel(
                app,
                schedulerProvider,
                navigator,
                eventLogger,
                song
        ) as T
    }

}