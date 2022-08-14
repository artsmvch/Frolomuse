package com.frolo.audiofx2.impl

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
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
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(getPrefsName(storageKey), Context.MODE_PRIVATE)
    }

    override fun getCurrentPreset(): EqualizerPreset? {
        return when(prefs.getInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_NONE)) {
            PRESET_TYPE_NATIVE -> {
                val keyName = prefs.getString(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME, null)
                if (keyName.isNullOrBlank()) {
                    null
                } else {
                    defaults.getNativePreset(keyName)
                }
            }
            PRESET_TYPE_CUSTOM -> {
                val id = prefs.getLong(KEY_LAST_USED_CUSTOM_PRESET_ID, -1)
                if (id == -1L) {
                    null
                } else {
                    val presets = queryPresets(
                        selection = DatabaseSchema.Presets.ID + " = ?",
                        selectionArgs = arrayOf<String>(id.toString())
                    )
                    presets.firstOrNull()
                }
            }
            else -> null
        }
    }

    override fun usePreset(preset: EqualizerPreset) {
        when (preset) {
            is NativePresetImpl -> {
                prefs.edit().apply {
                    putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_NATIVE)
                    putString(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME, preset.keyName)
                    remove(KEY_LAST_USED_CUSTOM_PRESET_ID)
                }.apply()
            }
            is EqualizerPreset.Custom -> {
                prefs.edit().apply {
                    putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_CUSTOM)
                    putLong(KEY_LAST_USED_CUSTOM_PRESET_ID, preset.id)
                    remove(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME)
                }.apply()
            }
            else -> {
                unusePreset()
            }
        }
    }

    internal fun unusePreset() {
        prefs.edit().apply {
            putInt(KEY_LAST_USED_PRESET_TYPE, PRESET_TYPE_NONE)
            remove(KEY_LAST_USED_CUSTOM_PRESET_ID)
            remove(KEY_LAST_USED_NATIVE_PRESET_KEY_NAME)
        }.apply()
    }

    override fun getAllPresets(): List<EqualizerPreset> {
        return defaults.getNativePresets() + queryPresets(null, null)
    }

    private fun queryPresets(selection: String?, selectionArgs: Array<String>?): List<EqualizerPreset> {
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
                            val preset = CustomPresetImpl(
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

    override fun createPreset(name: String, bandLevels: Map<Int, Int>): EqualizerPreset {
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
        return CustomPresetImpl(
            id = id,
            name = name,
            bandLevels = bandLevels.toMap(),
            timedAdded = timedAdded
        )
    }

    override fun deletePreset(preset: EqualizerPreset) {
        if (preset is EqualizerPreset.Custom && preset.isDeletable) {
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
        private const val KEY_LAST_USED_CUSTOM_PRESET_ID = "last_used_custom_preset_id"

        private const val PRESET_TYPE_NONE = -1
        private const val PRESET_TYPE_NATIVE = 0
        private const val PRESET_TYPE_CUSTOM = 1

        private fun getPrefsName(storageKey: String): String {
            return "$storageKey:audiofx2:presets"
        }
    }
}