package com.frolo.audiofx2.impl

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns


private const val SQL_CREATE_PRESETS = ("create table " + DatabaseSchema.PRESETS_TABLE_NAME + "("
        + DatabaseSchema.Presets.ID + " integer primary key, "
        + DatabaseSchema.Presets.NAME + " text, "
        + DatabaseSchema.Presets.LEVELS + " text, "
        + DatabaseSchema.Presets.TIME_ADDED + " long);")

private const val DATABASE_VERSION = 1
private fun getDatabaseName(storageKey: String): String {
    return "$storageKey.audiofx2.db"
}

internal object DatabaseSchema {
    const val PRESETS_TABLE_NAME = "presets"
    object Presets : BaseColumns {
        const val ID = BaseColumns._ID
        const val NAME = "name"
        const val LEVELS = "levels"
        const val TIME_ADDED = "time_added"
    }
}

internal class AudioEffect2DatabaseHelper(
    context: Context,
    storageKey: String
): SQLiteOpenHelper(context, getDatabaseName(storageKey), null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PRESETS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}