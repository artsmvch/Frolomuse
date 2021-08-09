package com.frolo.muse.database.entity

import androidx.room.*
import java.io.Serializable


/**
 * Database entity that represents a member of a playlist (see [PlaylistEntity]).
 *
 * The play order of playlist members is determined by their [prevId] and [nextId].
 *
 * [prevId] is [id] of the previous member. At the same time, [nextId] of the previous member is
 * [id] of this one. If [prevId] is null, then this is the first member in play order.
 *
 * [nextId] is [id] of the next member. At the same time, [prevId] of the next member is
 * [id] of this one. If [nextId] is null, then this is the last member in play order.
 */
@Entity(
    tableName = "playlist_members",
    indices = [
        Index(value = ["id", "playlist_id"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            childColumns = ["playlist_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class PlaylistMemberEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = NO_ID,
    @ColumnInfo(name = "prev_id")
    val prevId: Long? = null,
    @ColumnInfo(name = "next_id")
    val nextId: Long? = null,
    @ColumnInfo(name = "audio_id")
    val audioId: Long?,
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,
    @ColumnInfo(name = "source")
    val source: String?,
    @ColumnInfo(name = "date_added")
    val dateAdded: Long? = null,
    @ColumnInfo(name = "date_modified")
    val dateModified: Long? = null
): Serializable {
    companion object {
        const val NO_ID = 0L
    }
}