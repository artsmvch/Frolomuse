package com.frolo.muse.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frolo.muse.database.entity.PlaylistEntity
import com.frolo.muse.database.entity.PlaylistMemberEntity


@Database(
    version = 1,
    entities = [
        PlaylistEntity::class,
        PlaylistMemberEntity::class
    ],
    exportSchema = true
)
abstract class FrolomuseDatabase : RoomDatabase() {
    abstract fun getPlaylistEntityDao(): PlaylistEntityDao
    abstract fun getPlaylistMemberEntityDao(): PlaylistMemberEntityDao
}