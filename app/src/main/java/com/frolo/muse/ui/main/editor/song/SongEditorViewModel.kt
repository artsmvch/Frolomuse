package com.frolo.muse.ui.main.editor.song

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.common.toAudioSource
import com.frolo.player.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logSongUpdated
import com.frolo.music.model.Song
import com.frolo.music.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import io.reactivex.Completable
import org.cmc.music.metadata.MusicMetadata
import org.cmc.music.myid3.MyID3
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@Suppress("FunctionName")
class SongEditorViewModel constructor(
    application: Application,
    private val player: Player,
    private val schedulerProvider: SchedulerProvider,
    private val repository: SongRepository,
    private val eventLogger: EventLogger,
    private val songArg: Song
): BaseAndroidViewModel(application, eventLogger) {

    private val _title = MutableLiveData<String?>(songArg.title)
    val title: LiveData<String?> get() = _title

    private val _album = MutableLiveData<String?>(songArg.album)
    val album: LiveData<String?> get() = _album

    private val _artist = MutableLiveData<String?>(songArg.artist)
    val artist: LiveData<String?> get() = _artist

    private val _genre = MutableLiveData<String?>(songArg.genre)
    val genre: LiveData<String?> get() = _genre

    private val _handleWriteRequestEvent = SingleLiveEvent<Song>()
    val handleWriteRequestEvent: LiveData<Song> get() = _handleWriteRequestEvent

    private val _isLoadingUpdate: MutableLiveData<Boolean> = MutableLiveData()
    val isLoadingUpdate: LiveData<Boolean> get() = _isLoadingUpdate

    private val _updatedSong: MutableLiveData<Song> = MutableLiveData()
    val updatedSong: LiveData<Song> get() = _updatedSong

    private val _updateError: MutableLiveData<Throwable> = MutableLiveData()
    val updateError: LiveData<Throwable> get() = _updateError

    fun onTitleChanged(text: String?) {
        _title.value = text
    }

    fun onAlbumChanged(text: String?) {
        _album.value = text
    }

    fun onArtistChanged(text: String?) {
        _artist.value = text
    }

    fun onGenreChanged(text: String?) {
        _genre.value = text
    }

    fun onSaveClicked() {
        _handleWriteRequestEvent.value = songArg
    }

    fun onUserHandledWriteRequest() {
        doSave(
            title = title.value.orEmpty(),
            album = album.value.orEmpty(),
            artist = artist.value.orEmpty(),
            genre = genre.value.orEmpty()
        )
    }

    private fun doSave(
        title: String,
        album: String,
        artist: String,
        genre: String
    ) {
        Completable
            // First try updating tags using MyID3 lib
            .fromAction {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    updateTagsInternal_API29(song = songArg, newTitle = title, newAlbum = album, newArtist = artist, newGenre = genre)
                } else {
                    updateTagsInternal(song = songArg, newTitle = title, newAlbum = album, newArtist = artist, newGenre = genre)
                }
            }
            .andThen(repository.getSong(songArg.source))
            // if an error occurred then try updating tags using repository
            .onErrorResumeNext(repository.update(songArg, title, album, artist, genre))
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { _isLoadingUpdate.value = true }
            .doFinally { _isLoadingUpdate.value = false }
            .doOnSuccess { updatedSong ->
                player.update(updatedSong.toAudioSource())
                eventLogger.logSongUpdated()
            }
            .subscribe { newSong: Song?, err: Throwable? ->
                if (newSong != null) {
                    _updatedSong.value = newSong
                } else if (err != null) {
                    _updateError.value = err
                }
            }
            .save()
    }

    /**
     * Modifies metadata directly in the original song file.
     * This method is suitable Android API < 10.
     */
    @WorkerThread
    @Throws(Exception::class)
    private fun updateTagsInternal(
        song: Song,
        newTitle: String,
        newAlbum: String,
        newArtist: String,
        newGenre: String
    ) {
        val songFile = File(song.source)
        updateMetadata(file = songFile, newTitle = newTitle, newAlbum = newAlbum, newArtist = newArtist, newGenre = newGenre)

        // Scan the source audio file to update the song entity in the media store
        scanSync(songFile)
    }

    /**
     * This method differs from [updateTagsInternal] in that a copy of the original song file is created here,
     * which metadata is manipulated. The original song file is then overwritten with the modified copy.
     * This is due to restrictions on changing files in Android 10+.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @WorkerThread
    @Throws(Exception::class)
    private fun updateTagsInternal_API29(
        song: Song,
        newTitle: String,
        newAlbum: String,
        newArtist: String,
        newGenre: String
    ) {
        val context: Context = justApplication

        val sourceFile = File(song.source)
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.let { ContentUris.withAppendedId(it, song.id) }
        val songExtension: String = sourceFile.extension

        val tmpFilename = StringBuilder()
            .append("tmp_song_file")
            .append("_${System.currentTimeMillis()}")
            .run {
                if (songExtension.isNotBlank()) {
                    append('.').append(songExtension)
                } else {
                    this
                }
            }
            .toString()
        val tmpDir = File(context.cacheDir, "song_editor").apply { mkdirs() }
        val tmpFile = File(tmpDir, tmpFilename)
        val tmpFileUri = Uri.fromFile(tmpFile)

        // Copy the source audio file to the tmp audio file in the cache
        context.contentResolver.openOutputStream(tmpFileUri, "w")?.use { stream ->
            stream.write(sourceFile.readBytes())
        }

        // Updating tags of the tmp audio file in the cache
        updateMetadata(file = tmpFile, newTitle = newTitle, newAlbum = newAlbum, newArtist = newArtist, newGenre = newGenre)

        // Copy the tmp audio file to the source audio file
        context.contentResolver.openOutputStream(songUri, "w")?.use { stream ->
            stream.write(tmpFile.readBytes())
        }

        // Deleting the tmp file, because we don't need it anymore
        tmpFile.delete()

        // Scan the source audio file to update the song entity in the media store
        scanSync(sourceFile)
    }

    @WorkerThread
    private fun updateMetadata(
        file: File,
        newTitle: String,
        newAlbum: String,
        newArtist: String,
        newGenre: String
    ) {
        val myID3 = MyID3()
        val metadataSet = myID3.read(file) ?: throw NullPointerException("MusicMetadataSet is null")
        // Updating metadata
        val metadata = metadataSet.simplified as MusicMetadata
        metadata.songTitle = newTitle
        metadata.album = newAlbum
        metadata.artist = newArtist
        metadata.genre = newGenre
        myID3.update(file, metadataSet, metadata)
    }

    @WorkerThread
    private fun scanSync(file: File) {
        val countDownLatch = CountDownLatch(1)
        val paths = arrayOf(file.absolutePath)
        MediaScannerConnection.scanFile(justContext, paths, null) { _, _ ->
            // Scan completed, counting down the latch
            countDownLatch.countDown()
        }
        // Need to catch any InterruptedException
        // because the thread can be interrupted when the view model is cleared.
        try {
            // 30 seconds is maximum waiting time for the user
            countDownLatch.await(30, TimeUnit.SECONDS)
        } catch (ignored: InterruptedException) {
        }
    }

}