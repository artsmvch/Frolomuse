package com.frolo.muse.di.impl.local

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.palette.graphics.get
import com.frolo.core.graphics.Palette
import com.frolo.core.graphics.Swatch
import com.frolo.muse.common.albumId
import com.frolo.muse.graphics.PaletteGenerator
import com.frolo.player.AudioSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class PaletteGeneratorImpl(
    private val context: Context
) : PaletteGenerator {

    override fun generatePalette(audioSource: AudioSource): Single<Palette> {
        return Single.fromCallable { retrieveBitmap(audioSource) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { bmp -> androidx.palette.graphics.Palette.from(bmp) }
            .map { palette -> palette.generate() }
            .map { palette -> PaletteImpl(palette) }
    }

    private fun resolveArtUri(audioSource: AudioSource): Uri {
        return ContentUris.withAppendedId(URI, audioSource.albumId)
    }

    @Throws(Exception::class)
    @WorkerThread
    private fun retrieveBitmap(audioSource: AudioSource): Bitmap {
        val uri: Uri = resolveArtUri(audioSource)
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw NullPointerException("No input stream: uri=$uri")
        val bitmap = inputStream.use { stream ->
            val options = BitmapFactory.Options().apply {
                outWidth = RESOURCE_SIZE
                outHeight = RESOURCE_SIZE
            }
            BitmapFactory.decodeStream(stream, null, options)
        } ?: throw IllegalStateException("Failed to decode bitmap: uri=$uri")
        return bitmap
    }

    private class PaletteImpl(
        val delegate: androidx.palette.graphics.Palette
    ) : Palette {
        override fun getSwatch(target: Palette.Target): Swatch? {
            val delegateTarget = when (target) {
                Palette.Target.LIGHT_VIBRANT -> androidx.palette.graphics.Target.LIGHT_VIBRANT
                Palette.Target.VIBRANT ->       androidx.palette.graphics.Target.VIBRANT
                Palette.Target.DARK_VIBRANT ->  androidx.palette.graphics.Target.DARK_VIBRANT
                Palette.Target.LIGHT_MUTED ->   androidx.palette.graphics.Target.LIGHT_MUTED
                Palette.Target.MUTED ->         androidx.palette.graphics.Target.MUTED
                Palette.Target.DARK_MUTED ->    androidx.palette.graphics.Target.DARK_MUTED
            }
            return delegate[delegateTarget]?.let(::SwatchImpl)
        }
    }

    private class SwatchImpl(
        val delegate: androidx.palette.graphics.Palette.Swatch
    ): Swatch {
        override val rgb: Int get() = delegate.rgb
    }

    companion object {
        private const val URI_STRING = "content://media/external/audio/albumart"
        private val URI = Uri.parse(URI_STRING)

        private const val RESOURCE_SIZE = 40
    }
}