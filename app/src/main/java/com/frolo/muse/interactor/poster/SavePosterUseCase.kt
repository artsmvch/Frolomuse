package com.frolo.muse.interactor.poster

import android.content.Context
import android.graphics.Bitmap
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


/**
 * Saves bitmaps in the cache dir in the internal storage.
 */
class SavePosterUseCase @Inject constructor(
    private val context: Context,
    private val schedulerProvider: SchedulerProvider
) {

    /**
     * Saves [bmp] in the cache dir in the internal storage and returns the newly created file.
     * NOTE: [bmp] is not recycled in here.
     */
    fun savePoster(bmp: Bitmap): Single<File> =
        Single.fromCallable {
            val filename = "poster_" + System.currentTimeMillis() + ".png"
            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            file.setReadable(true, true)
            return@fromCallable file
        }.subscribeOn(schedulerProvider.worker())

}