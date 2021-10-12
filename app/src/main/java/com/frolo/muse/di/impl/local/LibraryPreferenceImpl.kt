package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import com.frolo.muse.model.media.SongFilter
import com.frolo.muse.model.media.SongType
import com.frolo.muse.repository.LibraryPreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


internal class LibraryPreferenceImpl(
    private val context: Context
) : LibraryPreferences {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val songTypeMapping = mapOf(
        SongType.MUSIC to "music",
        SongType.PODCAST to "podcast",
        SongType.RINGTONE to "ringtone",
        SongType.ALARM to "alarm",
        SongType.NOTIFICATION to "notification",
        SongType.AUDIOBOOK to "audiobook"
    )

    private val defaultSongFilter: SongFilter by lazy {
        SongFilter.Builder().addType(SongType.MUSIC).build()
    }

    override fun getSongFilter(): Flowable<SongFilter> {
        return RxPreference.ofString(prefs, KEY_SONG_FILTER)
            .get()
            .map { optional ->
                if (optional.isPresent) {
                    val builder = SongFilter.Builder()
                    val tokens = optional.get().split(SONG_TYPE_DELIMITER)
                    tokens.forEach { token ->
                        val entry = songTypeMapping.entries.firstOrNull { it.value == token }
                        if (entry != null) {
                            builder.addType(entry.key)
                        }
                    }
                    builder.build()
                } else {
                    defaultSongFilter
                }
            }
            .onErrorReturnItem(defaultSongFilter)
    }

    override fun setSongFilter(filter: SongFilter): Completable {
        return Single.fromCallable {
            val tokens = filter.types.mapNotNull { songTypeMapping.get(it) }
            tokens.joinToString(SONG_TYPE_DELIMITER)
        }
            .subscribeOn(Schedulers.computation())
            .flatMapCompletable { value ->
                RxPreference.ofString(prefs, KEY_SONG_FILTER).set(value)
            }
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.LibraryPreferences"

        private const val KEY_SONG_FILTER = "song_filter"

        // Must not be changed!!!
        private const val SONG_TYPE_DELIMITER = ","
    }

}