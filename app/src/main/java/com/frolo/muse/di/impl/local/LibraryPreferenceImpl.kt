package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import com.frolo.muse.model.media.SongFilter
import com.frolo.muse.model.media.SongType
import com.frolo.muse.repository.LibraryPreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable
import org.json.JSONArray
import org.json.JSONObject


internal class LibraryPreferenceImpl(
    private val context: Context
) : LibraryPreferences {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val defaultSongFilter: SongFilter by lazy {
        SongFilter.allEnabled()
    }

    private fun toJSON(filter: SongFilter): String {
        val json = JSONObject()

        // Types
        val typesArray = JSONArray()
        for (type in filter.types) {
            typesArray.put(SONG_TYPE_MAPPING[type])
        }
        json.put(JSON_KEY_SONG_TYPES, typesArray)

        // Min duration
        if (filter.minDuration != SongFilter.DURATION_NOT_SET) {
            json.put(JSON_KEY_MIN_DURATION, filter.minDuration)
        }

        return json.toString()
    }

    private fun toSongFilter(jsonString: String): SongFilter {
        val builder = SongFilter.Builder().noTypes()
        val json = JSONObject(jsonString)

        // Types
        json.optJSONArray(JSON_KEY_SONG_TYPES)?.also { typesArray ->
            for (i in 0 until typesArray.length()) {
                val value = typesArray.get(i)
                val entry = SONG_TYPE_MAPPING.entries.firstOrNull { it.value == value }
                if (entry != null) {
                    builder.addType(entry.key)
                }
            }
        }

        // Min duration
        if (json.has(JSON_KEY_MIN_DURATION)) {
            val minDuration = json.getLong(JSON_KEY_MIN_DURATION)
            builder.setMinDuration(minDuration)
        }

        return builder.build()
    }

    override fun getSongFilter(): Flowable<SongFilter> {
        return RxPreference.ofString(prefs, KEY_SONG_FILTER)
            .get()
            .map { optional ->
                if (optional.isPresent) {
                    toSongFilter(optional.get())
                } else {
                    defaultSongFilter
                }
            }
            .onErrorReturnItem(defaultSongFilter)
    }

    private fun updateSongFilter(updater: (SongFilter) -> SongFilter): Completable {
        return songFilter.first(defaultSongFilter)
            .flatMapCompletable { filter ->
                val newFilter = updater.invoke(filter)
                val newJson = toJSON(newFilter)
                RxPreference.ofString(prefs, KEY_SONG_FILTER).set(newJson)
            }
    }

    override fun getSongTypes(): Flowable<List<SongType>> {
        return songFilter.map { filter -> filter.types.toList() }
    }

    override fun setSongTypes(types: Collection<SongType>): Completable {
        return updateSongFilter { filter ->
            filter.newBuilder()
                .setOnlyTypes(types)
                .build()
        }
    }

    override fun getMinAudioDuration(): Flowable<Long> {
        return songFilter.map { filter -> filter.minDuration }
    }

    override fun setMinAudioDuration(duration: Long): Completable {
        return updateSongFilter { filter ->
            filter.newBuilder()
                .setMinDuration(duration)
                .build()
        }
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.LibraryPreferences"

        private const val KEY_SONG_FILTER = "song_filter"

        // JSON keys (Must not be changed!!!)
        private const val JSON_KEY_SONG_TYPES = "song_types"
        private const val JSON_KEY_MIN_DURATION = "min_duration"

        private val SONG_TYPE_MAPPING = mapOf(
            SongType.MUSIC          to "music",
            SongType.PODCAST        to "podcast",
            SongType.RINGTONE       to "ringtone",
            SongType.ALARM          to "alarm",
            SongType.NOTIFICATION   to "notification",
            SongType.AUDIOBOOK      to "audiobook"
        )
    }

}