package com.frolo.muse.di.impl.local

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.frolo.muse.R
import com.frolo.muse.model.media.MediaBucket
import com.frolo.muse.model.media.MediaFile
import com.frolo.muse.model.media.Song
import com.frolo.muse.model.sort.SortOrder
import com.frolo.muse.repository.MediaFileRepository
import com.frolo.muse.repository.SongRepository
import com.frolo.rxcontent.CursorMapper
import com.frolo.rxcontent.RxContent
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.Executor


@RequiresApi(Build.VERSION_CODES.Q)
internal class MediaFileRepositoryImpl(
    context: Context,
    private val queryExecutor: Executor,
    private val songRepository: SongRepository
) : BaseMediaRepository<MediaFile>(context), MediaFileRepository {

    private val contentResolver: ContentResolver get() = context.contentResolver

    override fun getAudioFiles(): Flowable<List<MediaFile>> {
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?"
        val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
        return RxContent.query(contentResolver, URI, PROJECTION, selection,
                selectionArgs, null, queryExecutor, CURSOR_MAPPER)
    }

    override fun getSortedAudioFiles(bucket: MediaBucket, sortOrder: String?): Flowable<List<MediaFile>> {
        val selection = MediaStore.Files.FileColumns.BUCKET_ID + " = ? AND " + MediaStore.Files.FileColumns.MEDIA_TYPE + " = ?"
        val selectionArgs = arrayOf(bucket.id.toString(), MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
        val validatedSortOrder = validateSortOrder(sortOrder)
        return RxContent.query(contentResolver, URI, PROJECTION, selection,
                selectionArgs, validatedSortOrder, queryExecutor, CURSOR_MAPPER)
    }

    override fun getAudioFiles(bucket: MediaBucket): Flowable<List<MediaFile>> {
        return getSortedAudioFiles(bucket, SORT_BY_FILENAME)
    }

    override fun blockingGetSortOrders(): List<SortOrder> {
        return collectSortOrders(
            createSortOrder(SORT_BY_FILENAME, R.string.sort_by_filename),
            createSortOrder(SORT_BY_DATE_ADDED, R.string.sort_by_date_added),
            createSortOrder(SORT_BY_DATE_MODIFIED, R.string.sort_by_date_modified)
        )
    }

    override fun getAllItems(): Flowable<List<MediaFile>> {
        return RxContent.query(contentResolver, URI, PROJECTION, null,
                null, null, queryExecutor, CURSOR_MAPPER)
    }

    override fun getAllItems(sortOrder: String?): Flowable<List<MediaFile>> {
        val selection = MediaStore.Files.FileColumns.DISPLAY_NAME + " = ?"
        val selectionArgs = arrayOf(sortOrder.orEmpty())
        val validatedSortOrder = validateSortOrder(sortOrder)
        return RxContent.query(contentResolver, URI, PROJECTION, selection,
                selectionArgs, validatedSortOrder, queryExecutor, CURSOR_MAPPER)
    }

    override fun getFilteredItems(filter: String?): Flowable<List<MediaFile>> {
        val selection = MediaStore.Files.FileColumns.DISPLAY_NAME + " = ?"
        val selectionArgs = arrayOf(filter.orEmpty())
        return RxContent.query(contentResolver, URI, PROJECTION, selection,
                selectionArgs, null, queryExecutor, CURSOR_MAPPER)
    }

    override fun getItem(id: Long): Flowable<MediaFile> {
        return RxContent.queryItem(contentResolver, URI, PROJECTION, id, queryExecutor, CURSOR_MAPPER)
    }

    override fun delete(item: MediaFile): Completable {
        return Del.deleteMediaFile(context, item)
    }

    override fun delete(items: Collection<MediaFile>): Completable {
        return Del.deleteMediaFiles(context, items)
    }

    override fun addToPlaylist(playlistId: Long, item: MediaFile): Completable {
        return collectSongs(item).flatMapCompletable { songs ->
            songRepository.addToPlaylist(playlistId, songs)
        }
    }

    override fun addToPlaylist(playlistId: Long, items: MutableCollection<MediaFile>): Completable {
        return collectSongs(items).flatMapCompletable { songs ->
            songRepository.addToPlaylist(playlistId, songs)
        }
    }

    override fun collectSongs(item: MediaFile): Single<List<Song>> {
        return songRepository.getItem(item.id)
            .firstOrError()
            .map { listOf(it) }
    }

    override fun collectSongs(items: MutableCollection<MediaFile>): Single<List<Song>> {
        return Single.defer {
            val sources = items.map { item ->
                songRepository.getItem(item.id).firstOrError()
            }
            Single.zip(sources) { array ->
                array.map { it as Song }
            }
        }
    }

    override fun getAllFavouriteItems(): Flowable<List<MediaFile>> {
        return Flowable.error(UnsupportedOperationException("Favourites are not supported for MediaFiles"))
    }

    override fun isFavourite(item: MediaFile?): Flowable<Boolean> {
        return Flowable.error(UnsupportedOperationException("Favourites are not supported for MediaFiles"))
    }

    override fun changeFavourite(item: MediaFile?): Completable {
        return Completable.error(UnsupportedOperationException("Favourites are not supported for MediaFiles"))
    }

    override fun isShortcutSupported(item: MediaFile?): Single<Boolean> {
        return Single.just(false)
    }

    override fun createShortcut(item: MediaFile?): Completable {
        return Completable.error(UnsupportedOperationException("Shortcuts are not supported for MediaFiles"))
    }

    companion object {

        const val SORT_BY_FILENAME = MediaStore.Files.FileColumns.DISPLAY_NAME + " COLLATE NOCASE ASC"
        const val SORT_BY_DATE_ADDED = MediaStore.Files.FileColumns.DATE_ADDED + " ASC"
        const val SORT_BY_DATE_MODIFIED = MediaStore.Files.FileColumns.DATE_MODIFIED + " ASC"

        private val URI = MediaStore.Files.getContentUri("external")

        private val PROJECTION: Array<String> = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        private val CURSOR_MAPPER = CursorMapper<MediaFile> { cursor ->
            MediaFile(
                id = cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                name = cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                bucketId = cursor.getLong(cursor.getColumnIndex(PROJECTION[2])),
                bucketName = cursor.getString(cursor.getColumnIndex(PROJECTION[3])),
                relativePath = cursor.getString(cursor.getColumnIndex(PROJECTION[4])),
                mimeType = cursor.getString(cursor.getColumnIndex(PROJECTION[5]))
            )
        }

        private fun validateSortOrder(sortOrder: String?): String? {
            return when (sortOrder) {
                // Legacy
                MyFileQuery.Sort.BY_FILENAME -> SORT_BY_FILENAME
                MyFileQuery.Sort.BY_DATE_MODIFIED -> SORT_BY_DATE_MODIFIED
                // Valid sort orders
                SORT_BY_FILENAME, SORT_BY_DATE_ADDED, SORT_BY_DATE_MODIFIED -> sortOrder
                // Unknown sort order
                else -> null
            }
        }
    }

}