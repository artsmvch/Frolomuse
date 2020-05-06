package com.frolo.muse.interactor.media.get

import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.Preferences
import io.reactivex.Flowable


/**
 * Excludes short songs from this source.
 * The minimum song duration will be [Preferences.getMinAudioFileDuration].
 */
fun <T: Song> Flowable<List<T>>.excludeShortSongs(preferences: Preferences): Flowable<List<T>> {
    val sources = listOf(this, preferences.minAudioFileDuration)
    return Flowable.combineLatest(sources) { arr ->
        val songs = arr[0] as List<T>
        val minDuration = arr[1] as Int
        if (minDuration > 0) {
            songs.filter { song -> song.duration / 1000 >= minDuration }
        } else songs
    }
}

/**
 * Does the same as [excludeShortSongs].
 */
fun <T: Media> Flowable<List<T>>.excludeShortAudioFiles(preferences: Preferences): Flowable<List<T>> {
    val sources = listOf(this, preferences.minAudioFileDuration)
    return Flowable.combineLatest(sources) { arr ->
        val mediaList = arr[0] as List<T>
        val minDuration = arr[1] as Int
        if (minDuration > 0) {
            mediaList.filter { media ->
                media !is Song || media.duration / 1000 >= minDuration
            }
        } else mediaList
    }
}