package com.frolo.muse.ui.main.player.lyrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.lyrics.Lyrics
import com.frolo.muse.model.media.Song
import com.frolo.muse.network.NetworkHelper
import com.frolo.muse.repository.LyricsRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel


class LyricsViewModel constructor(
        private val schedulerProvider: SchedulerProvider,
        private val networkHelper: NetworkHelper,
        private val localRepository: LyricsRepository,
        private val remoteRepository: LyricsRepository,
        private val eventLogger: EventLogger,
        private val songArg: Song
): BaseViewModel(eventLogger) {

    private val _songName: MutableLiveData<String> = MutableLiveData()
    val songName: LiveData<String> = _songName

    private val _lyrics: MutableLiveData<Lyrics> = MutableLiveData()
    val lyrics: LiveData<Lyrics> = _lyrics

    private val _isLoadingLyrics: MutableLiveData<Boolean> = MutableLiveData()
    val isLoadingLyrics: LiveData<Boolean> = _isLoadingLyrics

    private val _isSavingLyrics: MutableLiveData<Boolean> = MutableLiveData()
    val isSavingLyrics: LiveData<Boolean> = _isSavingLyrics

    private val _lyricsSavedEvent: MutableLiveData<Unit> = SingleLiveEvent()
    val lyricsSavedEvent: LiveData<Unit> = _lyricsSavedEvent

    private val _isEditable: MutableLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        addSource(lyrics) {
            value = true
        }
        addSource(error) {
            value = false
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

    init {
        _songName.value = songArg.title
        localRepository.getLyrics(songArg)
                .onErrorResumeNext(remoteRepository.getLyrics(songArg))
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isLoadingLyrics.value = true }
                .doFinally { _isLoadingLyrics.value = false }
                .subscribeFor { lyrics ->  _lyrics.value = lyrics }
    }

    fun onSaveButtonClicked(typedText: String) {
        localRepository.setLyrics(songArg, Lyrics(typedText))
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isSavingLyrics.value = true }
                .doFinally { _isSavingLyrics.value = false }
                .subscribeFor { _lyricsSavedEvent.value = Unit }
    }

}