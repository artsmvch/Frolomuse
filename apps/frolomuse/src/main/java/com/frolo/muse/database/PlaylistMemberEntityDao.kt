package com.frolo.muse.database

import androidx.annotation.WorkerThread
import androidx.room.*
import com.frolo.muse.database.entity.PlaylistMemberEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import org.jetbrains.annotations.TestOnly


@Dao
abstract class PlaylistMemberEntityDao {

    @Query("SELECT * FROM playlist_members WHERE playlist_id = :playlistId")
    abstract fun getAllPlaylistMemberEntities(playlistId: Long): Flowable<List<PlaylistMemberEntity>>

    @WorkerThread
    @Insert
    abstract fun blockingInsertPlaylistMemberEntity(entity: PlaylistMemberEntity): Long

    @WorkerThread
    @Update
    abstract fun blockingUpdatePlaylistMemberEntity(entity: PlaylistMemberEntity)

    @WorkerThread
    @Delete
    abstract fun blockingDeletePlaylistMemberEntity(entity: PlaylistMemberEntity)

    @WorkerThread
    @Query("""
        UPDATE playlist_members 
        SET prev_id = :newPrevId 
        WHERE id = :entityId 
        AND playlist_id = :playlistId
        """)
    abstract fun blockingUpdatePrevId(entityId: Long, playlistId: Long, newPrevId: Long?)

    @WorkerThread
    @Query("""
        UPDATE playlist_members 
        SET next_id = :newNextId 
        WHERE id = :entityId 
        AND playlist_id = :playlistId
        """)
    abstract fun blockingUpdateNextId(entityId: Long, playlistId: Long, newNextId: Long?)

    @Query("SELECT * FROM playlist_members WHERE playlist_id = :playlistId AND audio_id = :audioId")
    abstract fun blockingGetPlaylistMemberEntityByAudioId(playlistId: Long, audioId: Long): PlaylistMemberEntity?

    @Query("DELETE FROM playlist_members WHERE audio_id = :audioId")
    abstract fun deletePlaylistMemberEntityByAudioId(audioId: Long): Completable

    @Query("DELETE FROM playlist_members WHERE audio_id IN (:audioIds)")
    abstract fun deletePlaylistMemberEntitiesByAudioId(audioIds: Collection<Long>): Completable

    @TestOnly
    @Query("DELETE FROM playlist_members")
    abstract fun nuke(): Completable

    //region Test

    @Query("SELECT * FROM playlist_members WHERE id = :entityId")
    abstract fun blockingFindPlaylistMemberEntityById(entityId: Long): PlaylistMemberEntity?

    @Query("SELECT * FROM playlist_members WHERE playlist_id = :playlistId AND next_id is NULL")
    abstract fun blockingFindLastPlaylistMemberEntity(playlistId: Long): PlaylistMemberEntity?

    @WorkerThread
    @Transaction
    open fun blockingAddPlaylistMemberEntities(
        entities: List<PlaylistMemberEntity>,
        allowDuplicateAudio: Boolean
    ) {
        entities.forEach { entity ->
            val playlistId = entity.playlistId
            if (!allowDuplicateAudio && entity.audioId != null) {
                val existing = blockingGetPlaylistMemberEntityByAudioId(
                    playlistId = playlistId,
                    audioId = entity.audioId
                )
                if (existing != null) {
                    // continue
                    return@forEach
                }
            }
            val currLastMemberEntity = blockingFindLastPlaylistMemberEntity(playlistId)
            val newLastMemberEntity = entity.copy(prevId = currLastMemberEntity?.id)
            val id = blockingInsertPlaylistMemberEntity(newLastMemberEntity)
            currLastMemberEntity?.copy(nextId = id)?.also(::blockingUpdatePlaylistMemberEntity)
        }
    }

    @WorkerThread
    @Transaction
    open fun blockingRemoveMembersFromPlaylist(entities: List<PlaylistMemberEntity>) {
        entities.forEach { entity ->
            val prevEntity = entity.prevId?.let(::blockingFindPlaylistMemberEntityById)
            val nextEntity = entity.nextId?.let(::blockingFindPlaylistMemberEntityById)
            prevEntity?.copy(nextId = nextEntity?.id)?.also {
                blockingUpdatePlaylistMemberEntity(it)
            }
            nextEntity?.copy(prevId = prevEntity?.id)?.also {
                blockingUpdatePlaylistMemberEntity(it)
            }
            blockingDeletePlaylistMemberEntity(entity)
        }
    }

    @WorkerThread
    @Transaction
    open fun blockingMovePlaylistMemberEntity(
            target: PlaylistMemberEntity,
            previous: PlaylistMemberEntity?,
            next: PlaylistMemberEntity?) {

        // Check conditions

        if (previous != null && target.playlistId != previous.playlistId) {
            throw IllegalArgumentException("The previous entity is not from the same playlist")
        }

        if (next != null && target.playlistId != next.playlistId) {
            throw IllegalArgumentException("The next entity is not from the same playlist")
        }

        if (previous?.nextId != null && previous.nextId != next?.id) {
            throw IllegalArgumentException("The order {previous -> next} is messed")
        }

        if (next?.prevId != null && next.prevId != previous?.id) {
            throw IllegalArgumentException("The order {next <- previous} is messed")
        }

        // Detach the target from the old place

        val oldPrevious = target.prevId?.let { id ->
            blockingFindPlaylistMemberEntityById(id)!!
        }
        val oldNext = target.nextId?.let { id ->
            blockingFindPlaylistMemberEntityById(id)!!
        }
        oldPrevious?.also { entity ->
            blockingUpdateNextId(entityId = entity.id, playlistId = entity.playlistId, newNextId = oldNext?.id)
        }
        oldNext?.copy(prevId = oldPrevious?.id)?.also { entity ->
            blockingUpdatePrevId(entityId = entity.id, playlistId = entity.playlistId, newPrevId = oldPrevious?.id)
        }

        // Attach the target to the new place

        previous?.also { entity ->
            blockingUpdateNextId(entityId = entity.id, playlistId = entity.playlistId, newNextId = target.id)
        }
        next?.also { entity ->
            blockingUpdatePrevId(entityId = entity.id, playlistId = entity.playlistId, newPrevId = target.id)
        }
        target.copy(prevId = previous?.id, nextId = next?.id).also { entity ->
            blockingUpdatePlaylistMemberEntity(entity)
        }
    }

    //region
}