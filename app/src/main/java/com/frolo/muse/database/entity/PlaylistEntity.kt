package com.frolo.muse.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Database entity that represents a playlist.
 */
@Entity(
    tableName = "playlists"
)
data class PlaylistEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = NO_ID,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "source")
    val source: String?,
    @ColumnInfo(name = "date_created")
    val dateCreated: Long,
    @ColumnInfo(name = "date_modified")
    val dateModified: Long
) {
    companion object {
        const val NO_ID = 0L
    }
}