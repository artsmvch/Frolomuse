package com.frolo.muse.ui.main.player.poster

import android.graphics.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.interactor.poster.CreatePosterUseCase
import com.frolo.muse.interactor.poster.SavePosterUseCase
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import java.io.File


class PosterViewModel constructor(
    private val createPosterUseCase: CreatePosterUseCase,
    private val savePosterUseCase: SavePosterUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val navigator: Navigator,
    private val eventLogger: EventLogger,
    private val songArg: Song
): BaseViewModel(eventLogger) {

    private val _posterFile = MutableLiveData<File>()

    private val _isCreatingPoster = MutableLiveData<Boolean>()
    val isCreatingPoster: LiveData<Boolean> get() = _isCreatingPoster

    private val _poster: MutableLiveData<Bitmap> by lazy {
        // lazily do create poster
        MutableLiveData<Bitmap>().apply { doCreatePoster() }
    }
    val poster: LiveData<Bitmap> get() = _poster

    private fun doCreatePoster() {
        createPosterUseCase.createPoster(songArg)
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { _isCreatingPoster.value = true }
            .doFinally { _isCreatingPoster.value = false }
            .subscribeFor { bmp -> _poster.value = bmp }
    }

    fun onShareButtonClicked() {
        val bmp = _poster.value ?: return
        val file = _posterFile.value
        if (file != null) {
            // The file exists already no need to create it again
            navigator.sharePoster(songArg, file)
        } else {
            savePosterUseCase.savePoster(bmp)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSuccess { posterFile -> _posterFile.value = posterFile }
                .subscribeFor { posterFile -> navigator.sharePoster(songArg, posterFile) }
        }
    }

    override fun onCleared() {
        poster.value?.also {
            runCatching { it.recycle() }
        }
        super.onCleared()
    }

}