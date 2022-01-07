package com.frolo.muse.di.impl.stub

import com.frolo.muse.ThrowableUtils
import com.frolo.music.model.MediaBucket
import com.frolo.music.model.MediaFile
import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.muse.model.sort.SortOrder
import com.frolo.muse.repository.MediaFileRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class MediaFileRepositoryStub : MediaFileRepository {

    override fun getAudioFiles(): Flowable<List<MediaFile>> {
        return flowableError()
    }

    override fun getSortedAudioFiles(bucket: MediaBucket, sortOrder: String?): Flowable<List<MediaFile>> {
        return flowableError()
    }

    override fun getAudioFiles(bucket: MediaBucket): Flowable<List<MediaFile>> {
        return flowableError()
    }

    override fun isShortcutSupported(item: MediaFile?): Single<Boolean> {
        return singleError()
    }

    override fun addToPlaylist(playlist: Playlist, item: MediaFile?): Completable {
        return completableError()
    }

    override fun addToPlaylist(playlist: Playlist, items: MutableCollection<MediaFile>?): Completable {
        return completableError()
    }

    override fun getAllFavouriteItems(): Flowable<MutableList<MediaFile>> {
        return flowableError()
    }

    override fun getItem(id: Long): Flowable<MediaFile> {
        return flowableError()
    }

    override fun getFilteredItems(namePiece: String?): Flowable<MutableList<MediaFile>> {
        return flowableError()
    }

    override fun collectSongs(item: MediaFile?): Single<MutableList<Song>> {
        return singleError()
    }

    override fun collectSongs(items: MutableCollection<MediaFile>?): Single<MutableList<Song>> {
        return singleError()
    }

    override fun createShortcut(item: MediaFile?): Completable {
        return completableError()
    }

    override fun changeFavourite(item: MediaFile?): Completable {
        return completableError()
    }

    override fun getAllItems(): Flowable<MutableList<MediaFile>> {
        return flowableError()
    }

    override fun getAllItems(sortOrder: String?): Flowable<MutableList<MediaFile>> {
        return flowableError()
    }

    override fun getSortOrders(): Single<MutableList<SortOrder>> {
        return singleError()
    }

    override fun isFavourite(item: MediaFile?): Flowable<Boolean> {
        return flowableError()
    }

    override fun delete(item: MediaFile?): Completable {
        return completableError()
    }

    override fun delete(items: MutableCollection<MediaFile>?): Completable {
        return completableError()
    }

    private fun <T> singleError() = Single.error<T>(ThrowableUtils.notImplementedError())

    private fun <T> flowableError() = Flowable.error<T>(ThrowableUtils.notImplementedError())

    private fun completableError() = Completable.error(ThrowableUtils.notImplementedError())

}