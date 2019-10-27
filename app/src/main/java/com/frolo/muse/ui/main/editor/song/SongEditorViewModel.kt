package com.frolo.muse.ui.main.editor.song

import android.media.MediaScannerConnection.scanFile
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.App
import com.frolo.muse.engine.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import io.reactivex.Completable
import org.cmc.music.metadata.MusicMetadata
import org.cmc.music.myid3.MyID3
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class SongEditorViewModel constructor(
        application: App,
        private val player: Player,
        private val schedulerProvider: SchedulerProvider,
        private val repository: SongRepository,
        private val eventLogger: EventLogger,
        private val songArg: Song
): BaseAndroidViewModel(application, eventLogger) {

    private val _isLoadingUpdate: MutableLiveData<Boolean> = MutableLiveData()
    val isLoadingUpdate: LiveData<Boolean> = _isLoadingUpdate

    private val _updatedSong: MutableLiveData<Song> = MutableLiveData()
    val updatedSong: LiveData<Song> = _updatedSong

    private val _updateError: MutableLiveData<Throwable> = MutableLiveData()
    val updateError: LiveData<Throwable> = _updateError

    fun onSaveClicked(
            title: String,
            album: String,
            artist: String,
            genre: String) {

        Completable
                // First try updating tags using MyID3 lib
                .fromAction { updateTagsInternal(songArg, title, album, artist, genre) }
                .andThen(repository.getSong(songArg.source))
                // if an error occurred then try updating tags using repository
                .onErrorResumeNext(repository.update(songArg, title, album, artist, genre))
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isLoadingUpdate.value = true }
                .doFinally { _isLoadingUpdate.value = false }
                .doOnSuccess { updatedSong -> player.update(updatedSong) }
                .subscribe { newSong: Song?, err: Throwable? ->
                    if (newSong != null) {
                        _updatedSong.value = newSong
                    } else if (err != null) {
                        _updateError.value = err
                    }
                }
                .save()
    }

    @WorkerThread
    @Throws(Exception::class)
    private fun updateTagsInternal(
            song: Song,
            newTitle: String,
            newAlbum: String,
            newArtist: String,
            newGenre: String) {

        val myID3 = MyID3()

        val src = File(song.source)
        val srcSet = myID3.read(src) ?: throw NullPointerException("Source set is null")

        // Updating metadata according the given values
        val metadata = (srcSet.simplified as MusicMetadata).also { metadata ->
            metadata.songTitle = newTitle
            metadata.album = newAlbum
            metadata.artist = newArtist
            metadata.genre = newGenre
        }

        myID3.update(src, srcSet, metadata)

        // Then we need the media scanner to scan the file to update media store
        scanSync(src)
    }

    @WorkerThread
    @Throws(InterruptedException::class)
    private fun scanSync(file: File) {
        val countDownLatch = CountDownLatch(1)
        val context = getApplication<App>()
        val paths = arrayOf(file.absolutePath)
        scanFile(context, paths, null) { _, _ ->
            // Scan completed, counting down the latch
            countDownLatch.countDown()
        }
        countDownLatch.await(10, TimeUnit.SECONDS) // awaiting with timeout
    }
}