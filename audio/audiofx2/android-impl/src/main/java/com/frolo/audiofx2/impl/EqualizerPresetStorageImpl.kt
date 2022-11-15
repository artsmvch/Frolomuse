package com.frolo.audiofx2.impl

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import androidx.annotation.GuardedBy
import com.frolo.audiofx2.EqualizerPreset
import com.frolo.audiofx2.EqualizerPresetStorage
import org.json.JSONObject

private fun toJson(bandLevels: Map<Int, Int>): String? {
    return try {
        val json = JSONObject()
        bandLevels.entries.forEach { entry ->
            json.put(entry.key.toString(), entry.value)
        }
        return json.toString()
    } catch (ignored: Throwable) {
        null
    }
}

private fun toMap(text: String): Map<Int, Int>? {
    return try {
        val json = JSONObject(text)
        val map = HashMap<Int, Int>()
        json.keys().forEach { key ->
            val band = key.toInt()
            val level = json.getInt(key)
            map[band] = level
        }
        return map
    } catch (ignored: Throwable) {
        null
    }
}

internal class EqualizerPresetStorageImpl(
    private val context: Context,
    private val databaseHelper: AudioEffect2DatabaseHelper,
    private val storageKey: String,
    private val defaults: Defaults
): EqualizerPresetStorage {
    private val lock = Any()
    @get:GuardedBy("lock")
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(getPrefsName(storageKey), Context.MODE_PRIVATE)
    }
    @GuardedBy("lock")
    private val bandLevels = HashMap<Int, Int>()

    init {
        // FIXME: make this call lazy
        restoreState()
    }

    private fun restoreState() = synchronized(lock) {
        prefs.all?.entries?.forEach { entry ->
            if (entry.key.startsWith(KEY_CUSTOM_BAND_LEVEL_PREFIX)) {
                try {
                    val bandIndex = entry.key.substring(KEY_CUSTOM_BAND_LEVEL_PREFIX.length).toInt()
                    val bandLevel = entry.value?.toString()!!.toInt()
                    bandLevels[bandIndex] = bandLevel
                } catch (ignored: NumberFormatException) {
                }
            }
        }
    }

    internal fun getNumberOfBands(): Int = synchronized(lock) {
        bandLevels.size
    }

    internal fun getBandLevel(bandIndex: Int): Int = synchronized(lock) {
        bandLevels[bandIndex] ?: defaults.zeroBandLevel
    }

    internal fun setBandLevel(bandIndex: Int, bandLevel: Int): EqualizerPreset = synchronized(lock) {
        if (bandLevels.isEmpty()) {
            repeat(defaults.numberOfBands) { iteration ->
                bandLevels[iteration] = defaults.zeroBandLevel
            }
        }
        bandLevels[bandIndex] = bandLevel
        prefs.edit().apply {
            putInt(KEY_CUSTOM_BAND_LEVEL_PREFIX + bandIndex, bandLevel)
            putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_CUSTOM)
        }.apply()
        // FIXME: sync all levels with the current engine
        return@synchronized getCustomPreset()
    }

    override fun getCurrentPreset(): EqualizerPreset = synchronized(lock) {
        val presetType = prefs.getInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_CUSTOM)
        if (presetType == PRESET_TYPE_SAVED) {
            val id = prefs.getLong(KEY_LAST_USED_SAVED_PRESET_ID, -1L)
            if (id != -1L) {
                val presets = queryPresets(
                    selection = DatabaseSchema.Presets.ID + " = ?",
                    selectionArgs = arrayOf<String>(id.toString())
                )
                val savedPreset = presets.firstOrNull()
                if (savedPreset != null) {
                    return@synchronized savedPreset
                }
            }
        }
        if (presetType == PRESET_TYPE_NATIVE) {
            val keyName = prefs.getString(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME, null)
            val nativePreset = if (!keyName.isNullOrBlank()) {
                defaults.getNativePreset(keyName)
            } else {
                null
            }
            if (nativePreset != null) {
                return@synchronized nativePreset
            }
        }
        return@synchronized getCustomPreset()
    }

    private fun getCustomPreset(): EqualizerPreset.Custom {
        return CustomPresetImpl(name = context.getString(R.string.preset_custom))
    }

    override fun usePreset(preset: EqualizerPreset) = synchronized(lock) {
        when (preset) {
            is NativePresetImpl -> {
                prefs.edit().apply {
                    putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_NATIVE)
                    putString(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME, preset.keyName)
                    remove(KEY_LAST_USED_SAVED_PRESET_ID)
                }.apply()
            }
            is SavedPresetImpl -> {
                prefs.edit().apply {
                    putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_SAVED)
                    putLong(KEY_LAST_USED_SAVED_PRESET_ID, preset.id)
                    remove(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME)
                }.apply()
            }
            is CustomPresetImpl -> {
                prefs.edit().apply {
                    putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_CUSTOM)
                    remove(KEY_LAST_USED_SAVED_PRESET_ID)
                    remove(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME)
                }.apply()
            }
            else -> Unit
        }
    }

    override fun getAllPresets(): List<EqualizerPreset> = synchronized(lock) {
        return listOf(getCustomPreset()) +
                defaults.getNativePresets() +
                queryPresets(null, null)
    }

    private fun queryPresets(selection: String?, selectionArgs: Array<String>?): List<EqualizerPreset> = synchronized(lock) {
        return databaseHelper.readableDatabase.use { database ->
            val columns = arrayOf<String>(
                DatabaseSchema.Presets.ID,
                DatabaseSchema.Presets.NAME,
                DatabaseSchema.Presets.LEVELS,
                DatabaseSchema.Presets.TIME_ADDED
            )
            val cursor: Cursor = database.query(
                DatabaseSchema.PRESETS_TABLE_NAME, columns, selection,
                selectionArgs, null, null, null)
                ?: return emptyList()
            cursor.use {
                if (it.moveToFirst()) {
                    val  dstList = ArrayList<EqualizerPreset>(cursor.count)
                    do {
                        val levelsJson = it.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.Presets.LEVELS))
                        val levels = toMap(levelsJson)
                        if (levels != null && levels.isNotEmpty()) {
                            val preset = SavedPresetImpl(
                                id = it.getLong(cursor.getColumnIndexOrThrow(DatabaseSchema.Presets.ID)),
                                name = it.getString(cursor.getColumnIndexOrThrow(DatabaseSchema.Presets.NAME)),
                                bandLevels = levels,
                                timedAdded = it.getLong(cursor.getColumnIndexOrThrow(DatabaseSchema.Presets.TIME_ADDED))
                            )
                            dstList.add(preset)
                        }
                    } while (it.moveToNext())
                    dstList
                } else {
                    emptyList()
                }
            }
        }
    }

    override fun createPreset(name: String, bandLevels: Map<Int, Int>): EqualizerPreset = synchronized(lock) {
        if (name.isBlank()) {
            throw IllegalArgumentException(context.getString(R.string.create_preset_err_empty_name))
        }
        val timedAdded = System.currentTimeMillis()
        val id = databaseHelper.writableDatabase.use { database ->
            val contentValues = ContentValues(3)
            contentValues.put(DatabaseSchema.Presets.NAME, name)
            contentValues.put(DatabaseSchema.Presets.LEVELS, toJson(bandLevels))
            contentValues.put(DatabaseSchema.Presets.TIME_ADDED, timedAdded)
            database.insert(DatabaseSchema.PRESETS_TABLE_NAME, null, contentValues)
        }
        if (id == -1L) {
            throw IllegalStateException("No row was created in the database")
        }
        return SavedPresetImpl(
            id = id,
            name = name,
            bandLevels = bandLevels.toMap(),
            timedAdded = timedAdded
        )
    }

    override fun deletePreset(preset: EqualizerPreset) = synchronized(lock) {
        if (preset is EqualizerPreset.Saved && preset.isDeletable) {
            val deletedCount = databaseHelper.writableDatabase.use { database ->
                val whereClause = DatabaseSchema.Presets.ID + " = ?"
                val whereArgs = arrayOf<String>(preset.id.toString())
                database.delete(
                    DatabaseSchema.PRESETS_TABLE_NAME, whereClause, whereArgs)
            }
        } else {
            throw IllegalArgumentException("$preset is not deletable")
        }
    }

    companion object {
        private const val KEY_LAST_USED_PRESET_TYPE = "last_used_preset_type"
        private const val KEY_LAST_USED_NATIVE_PRESET_KEY_NAME = "last_used_native_preset_key_name"
        private const val KEY_LAST_USED_SAVED_PRESET_ID = "last_used_saved_preset_id"
        private const val KEY_CUSTOM_BAND_LEVEL_PREFIX = "custom_band_level_"

        private const val PRESET_TYPE_CUSTOM = -1
        private const val PRESET_TYPE_NATIVE = 0
        private const val PRESET_TYPE_SAVED = 1

        private fun getPrefsName(storageKey: String): String {
            return "$storageKey.audiofx2.equalizer_presets"
        }
    }
}