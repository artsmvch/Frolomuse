package com.frolo.muse.ui.main.editor.album

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.Features
import com.frolo.arch.support.SingleLiveEvent
import com.frolo.arch.support.call
import com.frolo.arch.support.map
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.core.ui.glide.makeAlbumArtRequestAsBitmap
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logAlbumUpdated
import com.frolo.music.model.Album
import com.frolo.music.repository.AlbumRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import io.reactivex.Single


class AlbumEditorViewModel constructor(
    private val frolomuseApp: FrolomuseApp,
    private val schedulerProvider: SchedulerProvider,
    private val repository: AlbumRepository,
    private val eventLogger: EventLogger,
    private val albumArg: Album
): BaseAndroidViewModel(frolomuseApp, eventLogger) {

    private val isEditorOptionAvailable = Features.isAlbumEditorFeatureAvailable()

    /**
     * A workaround for loading bitmaps with RxJava2.
     * Unfortunately, RxJava2 does not support nullable values.
     * So we can use this to wrap nullable bitmap and pass as a result.
     */
    private data class BitmapResult(val bitmap: Bitmap?)

    private var _activated: Boolean = false

    private val _pickedArtFilepath = MutableLiveData<String?>(null)

    private val originalArt: LiveData<Bitmap> = MutableLiveData<Bitmap>().apply {
        createAlbumArtSource(albumArg)
                .doOnSuccess { value = it.bitmap }
                .doOnError { value = null }
                .subscribeFor { }
    }

    private val _pickArtEvent = SingleLiveEvent<Unit>()
    val pickArtEvent: LiveData<Unit> get() = _pickArtEvent

    private val _art by lazy {
        MediatorLiveData<Bitmap>().apply {
            addSource(originalArt) { original ->
                value = original
            }
        }
    }
    val art: LiveData<Bitmap> get() = _art

    val artVisible: LiveData<Boolean> =
            art.map(true) { art -> art != null }

    val placeholderVisible: LiveData<Boolean> =
            art.map(false) { art -> art == null }

    val placeholderPickArtOptionVisible: LiveData<Boolean> = placeholderVisible.map(false) { placeholderVisible ->
        isEditorOptionAvailable && placeholderVisible == true
    }

    val pickArtOptionVisible: LiveData<Boolean> = art.map(false) { art ->
        isEditorOptionAvailable && art != null
    }

    val deleteArtOptionVisible: LiveData<Boolean> = originalArt.map(false) { original ->
        isEditorOptionAvailable && original != null
    }

    private val _saveArtOptionVisible = MutableLiveData<Boolean>(false)
    val saveArtOptionVisible: LiveData<Boolean>
        get() = _saveArtOptionVisible

    private val _artDeletionConfirmationVisible = MutableLiveData<Boolean>(false)
    val artDeletionConfirmationVisible: LiveData<Boolean>
        get() = _artDeletionConfirmationVisible

    private val _isSavingChanges = MutableLiveData<Boolean>(false)
    val isSavingChanges: LiveData<Boolean> get() = _isSavingChanges

    private val _artUpdatedEvent = SingleLiveEvent<Album>()
    val artUpdatedEvent: LiveData<Album> get() = _artUpdatedEvent

    fun onPickArtOptionClicked() {
        if (isEditorOptionAvailable) {
            _pickArtEvent.call()
        }
    }

    fun onArtPicked(filepath: String?) {
        _pickedArtFilepath.value = filepath
        _saveArtOptionVisible.value = true
        createFilepathSource(filepath)
                .doOnSuccess { _art.value = it.bitmap }
                .subscribeFor {  }
    }

    fun onDeleteArtClicked() {
        _artDeletionConfirmationVisible.value = true
    }

    fun onArtDeletionCanceled() {
        _artDeletionConfirmationVisible.value = false
    }

    fun onArtDeletionConfirmed() {
        doSaveChanges(null)
    }

    fun onSaveClicked() {
        val filepath = _pickedArtFilepath.value
        doSaveChanges(filepath)
    }

    private fun doSaveChanges(newFilepath: String?) {
        repository.updateArt(albumArg.id, newFilepath)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isSavingChanges.value = true }
                .doFinally { _isSavingChanges.value = false }
                .doOnComplete { eventLogger.logAlbumUpdated(albumArtDeleted = newFilepath == null) }
                .subscribeFor {
                    // This is important to invalidate the key!
                    // Fuckin' glide is not able to do it itself.
                    GlideAlbumArtHelper.get().invalidate(albumArg.id)
                    _artUpdatedEvent.value = albumArg
                }
    }

    private fun createAlbumArtSource(album: Album): Single<BitmapResult> {
        val request = Glide.with(frolomuseApp).makeAlbumArtRequestAsBitmap(album.id)
        return Single.fromCallable {
            try {
                val bitmap = request.submit().get()
                BitmapResult(bitmap)
            } catch (ignored: Throwable) {
                BitmapResult(null)
            }
        }
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
    }

    private fun createFilepathSource(filepath: String?): Single<BitmapResult> {
        return Single.fromCallable {
            try {
                val bitmap = Glide.with(frolomuseApp)
                        .asBitmap()
                        .load(filepath)
                        .submit()
                        .get()
                BitmapResult(bitmap)
            } catch (ignored: Throwable) {
                BitmapResult(null)
            }
        }
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
    }

}