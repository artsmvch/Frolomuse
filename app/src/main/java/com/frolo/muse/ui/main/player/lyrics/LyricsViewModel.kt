package com.frolo.muse.ui.main.player.lyrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.arch.liveDataOf
import com.frolo.muse.arch.map
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logFailedToGetLyrics
import com.frolo.muse.logger.logLyricsSaved
import com.frolo.muse.logger.logLyricsViewed
import com.frolo.muse.model.lyrics.Lyrics
import com.frolo.muse.model.media.Song
import com.frolo.muse.network.NetworkHelper
import com.frolo.muse.repository.LyricsLocalRepository
import com.frolo.muse.repository.LyricsRemoteRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single


class LyricsViewModel constructor(
    private val schedulerProvider: SchedulerProvider,
    private val networkHelper: NetworkHelper,
    private val localRepository: LyricsLocalRepository,
    private val remoteRepository: LyricsRemoteRepository,
    private val eventLogger: EventLogger,
    private val songArg: Song
): BaseViewModel(eventLogger) {

    val songName: LiveData<String> = liveDataOf(songArg.title)

    private val _isLoadingLyrics = MutableLiveData<Boolean>()
    val isLoadingLyrics: LiveData<Boolean> get() = _isLoadingLyrics

    val isLyricsVisible = isLoadingLyrics.map(true) { isLoading -> isLoading != true }

    private val _lyrics by lazy {
        MutableLiveData<Lyrics>().apply {
            fetchLyrics()
        }
    }
    private val lyrics: LiveData<Lyrics> get() = _lyrics

    private val _isLyricsEdited = MutableLiveData<Boolean>(false)

    private val _lyricsText by lazy {
        MediatorLiveData<String>().apply {
            addSource(lyrics) { lyrics ->
                value = lyrics?.text
            }
        }
    }
    val lyricsText: LiveData<String> get() = _lyricsText

    private val _isSavingLyrics = MutableLiveData<Boolean>()
    val isSavingLyrics: LiveData<Boolean> get() = _isSavingLyrics

    private val _lyricsSavedEvent = SingleLiveEvent<Unit>()
    val lyricsSavedEvent: LiveData<Unit> get() = _lyricsSavedEvent

    private val _isEditable: MutableLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(lyrics) {
            value = true
        }
        addSource(isLoadingLyrics) { isLoading ->
            value = if (isLoading) {
                false
            } else {
                lyrics.value != null
            }
        }
    }
    val isEditable: LiveData<Boolean> = _isEditable

    private fun fetchLyrics() {
        // First, trying to fetch lyrics from the local repo
        localRepository.getLyrics(songArg)
            .subscribeOn(schedulerProvider.worker())
            .onErrorResumeNext {
                // In case of any error, fetching lyrics remotely and saving it to the local repo
                remoteRepository.getLyrics(songArg)
                    .subscribeOn(schedulerProvider.worker())
                    .doOnError { err -> eventLogger.logFailedToGetLyrics(songArg, err) }
                    .flatMap { lyrics ->
                        localRepository.setLyrics(songArg, lyrics)
                            .subscribeOn(schedulerProvider.worker())
                            .andThen(Single.just(lyrics))
                    }
            }
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { _isLoadingLyrics.value = true }
            .doFinally { _isLoadingLyrics.value = false }
            .doOnSuccess { eventLogger.logLyricsViewed() }
            .subscribeFor { lyrics -> _lyrics.value = lyrics }
    }

    fun onLyricsEdited(text: String?) {
        _isLyricsEdited.value = true
        _lyricsText.value = text
    }

    fun onSaveButtonClicked() {
        val typedText = lyricsText.value.orEmpty()
        val lyrics = Lyrics(typedText)
        localRepository.setLyrics(songArg, lyrics)
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { _isSavingLyrics.value = true }
            .doFinally { _isSavingLyrics.value = false }
            .doOnComplete { eventLogger.logLyricsSaved(_isLyricsEdited.value ?: false) }
            .subscribeFor { _lyricsSavedEvent.value = Unit }
    }

}