package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.Preferences
import io.reactivex.Flowable


fun Flowable<List<Song>>.excludeShortAudioFiles(preferences: Preferences): Flowable<List<Song>> {
    val sources = listOf(this, preferences.minAudioFileDuration)
    return Flowable.combineLatest(sources) { arr ->
        val songs = arr[0] as List<Song>
        val minDuration = arr[1] as Int
        if (minDuration > 0) {
            songs.filter { song -> song.duration / 1000 >= minDuration }
        } else songs
    }
}