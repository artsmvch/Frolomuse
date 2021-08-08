package com.frolo.muse.di.impl.local

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.room.Room
import com.frolo.muse.BuildConfig
import com.frolo.muse.LocalizedMessageException
import com.frolo.muse.R
import com.frolo.muse.database.FrolomuseDatabase
import com.frolo.muse.database.entity.*
import com.frolo.muse.kotlin.contains
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.media.Songs
import com.frolo.rxcontent.CursorMapper
import com.frolo.rxcontent.RxContent
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.Executors


/**
 * Playlist database wrapper that manages local storage for playlists.
 * The queried playlist models from this manager have [Playlist.isFromSharedStorage] set to true.
 */
internal class PlaylistDatabaseManager private constructor(private val context: Context) {

    // Executors and schedulers
    private val workerScheduler: Scheduler by lazy { Schedulers.io() }
    private val computationScheduler: Scheduler by lazy { Schedulers.computation() }
    private val queryExecutor by lazy { Executors.newCachedThreadPool() }

    // Database
    private val database: FrolomuseDatabase by lazy {
        Room.databaseBuilder(context, FrolomuseDatabase::class.java, DATABASE_NAME)
            .setQueryExecutor(queryExecutor)
            .build()
    }

    // Dao
    private val playlistEntityDao by lazy { database.getPlaylistEntityDao() }
    private val playlistMemberEntityDao by lazy { database.getPlaylistMemberEntityDao() }

    // Mappers
    private val entityToPlaylistMapper: (PlaylistEntity) -> Playlist = { entity ->
        Playlist(entity.id, false, entity.source,
                entity.name, entity.dateCreated, entity.dateModified)
    }

    /**
     * Queries a playlist with the given [id].
     */
    fun queryPlaylist(id: Long): Flowable<Playlist> {
        return playlistEntityDao
            .getPlaylistEntities(id)
            .map { list -> list.first() }
            .map(entityToPlaylistMapper)
    }

    /**
     * Queries all playlists stored in the local database. The returned list is sorted by [sortOrder].
     */
    fun queryAllPlaylists(sortOrder: String?): Flowable<List<Playlist>> {
        return playlistEntityDao
            .getAllPlaylistEntities()
            .observeOn(computationScheduler)
            .map { entities -> entities.map(entityToPlaylistMapper) }
            .map { playlists ->
                when (sortOrder) {
                    PlaylistQuery.Sort.BY_NAME -> playlists.sortedBy { it.name }
                    PlaylistQuery.Sort.BY_DATE_ADDED -> playlists.sortedBy { it.dateAdded }
                    PlaylistQuery.Sort.BY_DATE_MODIFIED -> playlists.sortedBy { it.dateModified }
                    else -> playlists
                }
            }
    }

    /**
     * Queries playlists stored in the local database. [filter] is used to filter playlists by [Playlist.getName].
     */
    fun queryAllPlaylistsFiltered(filter: String): Flowable<List<Playlist>> {
        return playlistEntityDao.getAllPlaylistEntitiesFiltered(filter)
            .observeOn(computationScheduler)
            .map { entities -> entities.map(entityToPlaylistMapper) }
    }

    /**
     * Creates a new playlist with [name]. The playlist info will be stored in a local database.
     */
    fun createPlaylist(name: String): Single<Playlist> {
        if (name.isBlank()) {
            val exception = LocalizedMessageException(context, R.string.name_is_empty)
            return Single.error(exception)
        }
        return playlistEntityDao
            .findPlaylistEntitiesByName(name)
            .flatMap { existingPlaylists ->
                if (existingPlaylists.isNotEmpty()) {
                    val exception = LocalizedMessageException(context, R.string.such_name_already_exists)
                    Single.error(exception)
                } else {
                    val timeMillis = System.currentTimeMillis()
                    val timeSeconds = timeMillis / 1000
                    val entity = PlaylistEntity(
                        name = name,
                        source = null,
                        dateCreated = timeSeconds,
                        dateModified = timeSeconds
                    )
                    playlistEntityDao.createPlaylistEntity(entity)
                        .map { id -> entity.copy(id = id) }
                        .map(entityToPlaylistMapper)
                }
            }
    }

    fun transferPlaylists(opList: List<PlaylistTransfer.Op>): Single<List<PlaylistTransfer.Result>> {
        return Single.fromCallable {
            val results = ArrayList<PlaylistTransfer.Result>(opList.size)
            opList.forEach { op ->
                val original: Playlist = op.original
                try {
                    // Atomicity is important
                    database.runInTransaction {
                        val name: String? = original.name
                        val songs: List<Song> = op.songs

                        // Checking if the name is correct
                        if (name.isNullOrBlank()) return@runInTransaction

                        // Checking for existing playlists with the same name
                        val existingPlaylists = playlistEntityDao.blockingFindPlaylistEntitiesByName(name)
                        if (existingPlaylists.isNotEmpty()) return@runInTransaction

                        // Creating playlist entity
                        val timeMillis = System.currentTimeMillis()
                        val timeSeconds = timeMillis / 1000
                        val playlistEntity = PlaylistEntity(
                            name = name,
                            source = null,
                            dateCreated = timeSeconds,
                            dateModified = timeSeconds
                        )
                        val playlistId: Long = playlistEntityDao.blockingCreatePlaylistEntity(playlistEntity)

                        // Adding playlist members
                        val memberEntities: List<PlaylistMemberEntity> = songs.map { song ->
                            PlaylistMemberEntity(
                                audioId = song.id,
                                source = song.source,
                                playlistId = playlistId
                            )
                        }
                        playlistMemberEntityDao.blockingAddPlaylistMemberEntities(
                            entities = memberEntities,
                            allowDuplicateAudio = false
                        )

                        playlistEntity.copy(id = playlistId)
                            .let(entityToPlaylistMapper)
                            .let { createdPlaylist ->
                                val result = PlaylistTransfer.Result(original, createdPlaylist)
                                results.add(result)
                            }
                    }
                } catch (err: Throwable) {
                    // So strict for debug
                    if (DEBUG) throw err
                    val result = PlaylistTransfer.Result(original, null)
                    results.add(result)
                }
            }

            // Result
            results.toList()
        }.subscribeOn(workerScheduler)
    }

    /**
     * Updates the name of [playlist] to [newName]. The updated info is stored in a local database.
     * Returns an error if the playlist is from the shared storage.
     */
    fun updatePlaylist(playlist: Playlist, newName: String): Single<Playlist> {
        if (playlist.isFromSharedStorage) {
            val err = IllegalArgumentException(getSharedStorageErrorLabel("update", playlist))
            return Single.error(err)
        }

        val entity = PlaylistEntity(
            id = playlist.id,
            name = newName,
            source = playlist.source,
            dateCreated = playlist.dateAdded,
            dateModified = playlist.dateModified
        )
        return playlistEntityDao.updatePlaylistEntity(entity)
            // Return the updated playlist model after successful update in DAO
            .andThen(Single.just(
                Playlist(playlist.id, playlist.isFromSharedStorage, playlist.source,
                 playlist.name, playlist.dateAdded, playlist.dateModified)
            ))
    }

    private fun getSharedStorageErrorLabel(operation: String, playlist: Playlist): String {
        return "Unable to process playlist from the shared storage: " +
                "operation=$operation, playlist=$playlist"
    }

    /**
     * Deletes [playlist] from the local database. Returns an error,
     * if the playlist is from the shared storage.
     */
    fun deletePlaylist(playlist: Playlist): Completable {
        if (playlist.isFromSharedStorage) {
            val err = IllegalArgumentException(getSharedStorageErrorLabel("deletion", playlist))
            return Completable.error(err)
        }

        return playlistEntityDao.deletePlaylistEntityById(playlist.id)
    }

    /**
     * Deletes [playlists] from the local database. Returns an error,
     * if any of the playlists is from the shared storage.
     */
    fun deletePlaylists(playlists: Collection<Playlist>): Completable {
        return Single.fromCallable {
            playlists.map { playlist ->
                if (playlist.isFromSharedStorage) {
                    throw IllegalArgumentException(getSharedStorageErrorLabel("deletion", playlist))
                }
                playlist.id
            } }
                .subscribeOn(computationScheduler)
                .flatMapCompletable { ids ->
                    playlistEntityDao.deletePlaylistEntitiesByIds(ids)
                }
    }

    /**
     * Queries song members of the playlist with the given [playlistId].
     */
    fun queryPlaylistMembers(playlistId: Long, sortOrder: String?): Flowable<List<Song>> {
        return playlistMemberEntityDao.getAllPlaylistMemberEntities(playlistId)
            .observeOn(computationScheduler)
            .doOnNext { entities -> if (DEBUG) detectPlaylistAnomalies(entities) }
            .switchMap { entities ->
                querySongs(entities)
                    .observeOn(workerScheduler)
                    .map { songs -> transformAndCleanUp(entities, songs) }
            }
            .observeOn(computationScheduler)
            .map { songs ->
                when (sortOrder) {
                    SongQuery.Sort.BY_PLAY_ORDER -> songs
                    SongQuery.Sort.BY_TITLE -> songs.sortedBy { it.title }
                    SongQuery.Sort.BY_ALBUM -> songs.sortedBy { it.album }
                    SongQuery.Sort.BY_ARTIST -> songs.sortedBy { it.artist }
                    SongQuery.Sort.BY_DURATION -> songs.sortedBy { it.duration }
                    SongQuery.Sort.BY_TRACK_NUMBER -> songs.sortedBy { it.trackNumber }
                    else -> songs
                }
            }
    }

    fun queryPlaylistMembers(playlistId: Long): Flowable<List<Song>> {
        return queryPlaylistMembers(playlistId, SongQuery.Sort.BY_PLAY_ORDER)
    }

    fun addPlaylistMembers(playlistId: Long, songs: Collection<Song>): Completable {
        return Completable.fromAction {
            val entities = songs.map { song ->
                PlaylistMemberEntity(
                    audioId = song.id,
                    source = song.source,
                    playlistId = playlistId
                )
            }
            playlistMemberEntityDao.blockingAddPlaylistMemberEntities(
                entities = entities,
                allowDuplicateAudio = false
            )
        }.subscribeOn(workerScheduler)
    }

    fun addPlaylistMember(playlistId: Long, song: Song): Completable {
        return addPlaylistMembers(playlistId, listOf(song))
    }

    /**
     * Removes [songs] from the playlist with [playlistId]. [!] Only models of songs that were
     * previously returned by [PlaylistDatabaseManager] are allowed as [songs] parameter.
     */
    fun removePlaylistMembers(playlistId: Long, songs: Collection<Song>): Completable {
        return Single.fromCallable { songs.map { (it as PlaylistMemberSong).entity } }
            .subscribeOn(computationScheduler)
            .observeOn(workerScheduler)
            .doOnSuccess { entities ->
                playlistMemberEntityDao.blockingRemoveMembersFromPlaylist(entities)
            }
            .ignoreElement()
    }

    fun removePlaylistMember(playlistId: Long, song: Song): Completable {
        return removePlaylistMembers(playlistId, listOf(song))
    }

    /**
     * Queries songs for [entities]. It is possible that for some entities songs are not found.
     * This is probably because they were deleted from the device.
     */
    private fun querySongs(entities: List<PlaylistMemberEntity>): Flowable<List<Song>> {
        val filteredEntities = entities.filter { entity ->
            !entity.source.isNullOrBlank()
        }
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selectionBuilder = StringBuilder(MediaStore.Audio.Media.DATA + " IN (")
        val selectionArgsBuilder = ArrayList<String>(filteredEntities.size)
        var firstItemsLooped = false
        for (entity in filteredEntities) {
            val path = requireNotNull(entity.source)
            if (firstItemsLooped) {
                selectionBuilder.append(',')
            }
            selectionBuilder.append('?')
            selectionArgsBuilder.add(path)
            firstItemsLooped = true
        }
        if (!firstItemsLooped) {
            // No params
            return Flowable.just(emptyList())
        }
        selectionBuilder.append(')')
        val selection: String = selectionBuilder.toString()
        val selectionArgs: Array<String>? = selectionArgsBuilder.toTypedArray()
        val sortOrder: String? = null
        return RxContent.query(context.contentResolver, uri, SONG_PROJECTION, selection,
                selectionArgs, sortOrder, queryExecutor, SONG_CURSOR_MAPPER)
    }

    @WorkerThread
    private fun transformAndCleanUp(entities: List<PlaylistMemberEntity>, songs: Collection<Song>): List<Song> {
        if (entities.isEmpty()) return emptyList()

        // Step 1: arrange entities in natural order
        val firstItem = entities.find { entity -> entity.prevId == null }
                ?: throw IllegalStateException("Could not find the first item in play order")
        val orderedEntities = ArrayList<PlaylistMemberEntity>(entities.size)
        var currItem: PlaylistMemberEntity? = firstItem
        while (currItem != null) {
            if (DEBUG) {
                if (orderedEntities.contains { it.id == currItem?.id }) {
                    throw PlaylistAnomaly("Found duplicates!!1")
                }
            }
            orderedEntities.add(currItem)

            val nextItem = entities.find { entity -> entity.id == currItem!!.nextId }
            if (nextItem == currItem) {
                if (DEBUG) {
                    throw PlaylistAnomaly("Inconsistency detected: " +
                            "the next item in play order == the current item in play order")
                }
                break
            }
            currItem = nextItem
        }

        // Step 2: find invalid entities and delete them from the dao
        val invalidEntities = orderedEntities.filter { entity ->
            // If it is null, then the desired audio may have been probably deleted from the device
            songs.find { song -> song.id == entity.audioId } == null
        }
        playlistMemberEntityDao.blockingRemoveMembersFromPlaylist(invalidEntities)

        // Step 3: rearrange entities again according to the invalid ones
        val finalOrderedEntities = ArrayList(orderedEntities)
        for (invalid in invalidEntities) {
            val index = finalOrderedEntities.indexOfFirst { it.id == invalid.id }
            if (index >= 0) {
                // here, we remove the invalid entity and bind its previous and next entities
                val prevIndex = index - 1
                val nextIndex = index + 1
                val prev = finalOrderedEntities.getOrNull(prevIndex)
                val next = finalOrderedEntities.getOrNull(nextIndex)
                prev?.copy(nextId = next?.id)?.also { updatedPrev ->
                    finalOrderedEntities[prevIndex] = updatedPrev
                }
                next?.copy(prevId = prev?.id)?.also { updatedNext ->
                    finalOrderedEntities[nextIndex] = updatedNext
                }
                finalOrderedEntities.removeAt(index)
            }
        }

        // Step 4: transform entities to songs
        val orderedSongs = ArrayList<Song>(orderedEntities.size)
        finalOrderedEntities.mapTo(orderedSongs) { entity ->
            val song: Song? = songs.find { song -> song.id == entity.audioId }
            if (song == null && DEBUG) {
                // Actually, this should not happen. If so,
                // then something is wrong in steps 2 or 3
                throw IllegalStateException("No song found for playlist member")
            }
            PlaylistMemberSong(song ?: songs.last(), entity.playlistId, entity)
        }

        // Step 5: enjoy the result
        return orderedSongs
    }

    fun movePlaylistMember(targetSong: Song, prevSong: Song?, nextSong: Song?): Completable {
        return Completable.fromAction {
            val target = (targetSong as PlaylistMemberSong).entity
            val prev = if (prevSong != null) (prevSong as PlaylistMemberSong).entity else null
            val next = if (nextSong != null) (nextSong as PlaylistMemberSong).entity else null
            playlistMemberEntityDao.blockingMovePlaylistMemberEntity(
                target = target,
                previous = prev,
                next = next
            )
        }.subscribeOn(workerScheduler)
    }

    @TestOnly
    fun nuke(): Completable {
        return Completable.concat(listOf(playlistMemberEntityDao.nuke(), playlistEntityDao.nuke()))
    }

    private data class PlaylistMemberSong(
        val song: Song,
        val playlistId: Long,
        val entity: PlaylistMemberEntity
    ) : Song {
        override fun getId(): Long = song.id
        override fun getSource(): String? = song.source
        override fun getTitle(): String? = song.title
        override fun getAlbumId(): Long = song.albumId
        override fun getAlbum(): String? = song.album
        override fun getArtistId(): Long = song.artistId
        override fun getArtist(): String? = song.artist
        override fun getGenre(): String? = song.genre
        override fun getDuration(): Int = song.duration
        override fun getYear(): Int = song.year
        override fun getTrackNumber(): Int = song.trackNumber
        override fun getKind(): Int = song.kind
    }

    companion object {

        private val DEBUG = BuildConfig.DEBUG

        const val DATABASE_NAME = "com.frolo.muse.MediaDatabase.sql"

        private val SONG_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        private val SONG_CURSOR_MAPPER = CursorMapper<Song> { cursor ->
            Songs.create(
                cursor.getLong(cursor.getColumnIndex(SONG_PROJECTION[0])),
                cursor.getString(cursor.getColumnIndex(SONG_PROJECTION[1])),
                cursor.getString(cursor.getColumnIndex(SONG_PROJECTION[2])),
                cursor.getLong(cursor.getColumnIndex(SONG_PROJECTION[3])),
                cursor.getString(cursor.getColumnIndex(SONG_PROJECTION[4])),
                cursor.getLong(cursor.getColumnIndex(SONG_PROJECTION[5])),
                cursor.getString(cursor.getColumnIndex(SONG_PROJECTION[6])),
                "",
                cursor.getInt(cursor.getColumnIndex(SONG_PROJECTION[7])),
                cursor.getInt(cursor.getColumnIndex(SONG_PROJECTION[8])),
                cursor.getInt(cursor.getColumnIndex(SONG_PROJECTION[9]))
            )
        }

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: PlaylistDatabaseManager? = null

        @AnyThread
        @JvmStatic
        fun get(context: Context): PlaylistDatabaseManager {
            var currentInstance: PlaylistDatabaseManager? = instance
            if (currentInstance != null) {
                return currentInstance
            }

            synchronized(this) {
                currentInstance = instance
                if (currentInstance == null) {
                    val applicationContext = context.applicationContext
                    currentInstance = PlaylistDatabaseManager(applicationContext)
                    instance = currentInstance
                }
                return currentInstance!!
            }
        }
    }

}