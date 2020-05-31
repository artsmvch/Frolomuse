package com.frolo.muse.interactor.poster

import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.renderscript.RSRuntimeException
import androidx.core.content.ContextCompat
import com.frolo.muse.R
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.getAlbumString
import com.frolo.muse.ui.getNameString
import com.frolo.muse.util.BitmapUtil
import io.reactivex.Single
import jp.wasabeef.glide.transformations.internal.FastBlur
import jp.wasabeef.glide.transformations.internal.RSBlur
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min


/**
 * Creates [Bitmap] poster for a song.
 */
class CreatePosterUseCase @Inject constructor(
    private val context: Context,
    private val schedulerProvider: SchedulerProvider
) {

    fun createPoster(song: Song): Single<Bitmap> =
        Single.fromCallable {
            blockingCreatePoster(song)
        }.subscribeOn(schedulerProvider.worker())

    private fun blockingCreatePoster(song: Song): Bitmap {
        val res = context.resources

        val albumId = song.albumId
        val appName = res.getString(R.string.app_name)
        val songName = song.getNameString(context.resources)
        val albumName = song.getAlbumString(context.resources)

        val art = blockingGetArt(albumId)

        // preparing size for poster and its elements
        val iconMarginLeft = 280f
        val iconMarginTop = 35f
        val iconSize = 80
        val posterMargin = 150
        val artDesiredWidth = 650
        val artDesiredHeight = 650
        val posterWidth = artDesiredWidth + 2 * posterMargin
        val posterHeight = artDesiredHeight + 2 * posterMargin
        val desiredSongNameTextWidth = 800f
        val desiredAlbumNameTextWidth = 600f
        val desiredAppNameTextWidth = posterWidth - 2 * iconMarginLeft - iconSize - /*margin between app icon and app name*/ 10

        val bmpWidth = art.width - 1
        val bmpHeight = art.height - 1
        val bmpSize = min(bmpWidth, bmpHeight)
        val x = (bmpWidth - bmpSize) / 2
        val y = (bmpHeight - bmpSize) / 2
        val cropped = Bitmap.createBitmap(art, x, y, bmpSize, bmpSize)
        val src = Bitmap.createScaledBitmap(cropped, posterWidth, posterHeight, true)
        val scaledSrc = Bitmap.createScaledBitmap(cropped, artDesiredWidth, artDesiredHeight, true)
        art.recycle()
        cropped.recycle()

        // Original paint
        val originalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            color = Color.WHITE
            strokeWidth = 5f
        }

        // Result bitmap
        val poster = applyBlurEffect(src, 25)!!

        // Canvas
        val canvas = Canvas(poster)

        // Overlay
        ContextCompat.getColor(context, R.color.transparent_black).also { overlayColor ->
            canvas.drawColor(overlayColor)
        }

        // Not blurred art in the center
        scaledSrc.also { bitmap ->
            canvas.drawBitmap(bitmap, posterMargin.toFloat(), posterMargin.toFloat(), originalPaint)
            bitmap.recycle()
        }

        // Song name
        Paint(originalPaint).also { paint ->
            paint.adjustTextSize(songName, desiredSongNameTextWidth, 52f)
            val textBounds = Rect()
            paint.getTextBounds(songName, 0, songName.length, textBounds)
            val textX = (posterWidth - textBounds.width()) / 2f
            val textY = artDesiredHeight + posterMargin * 1.34f + textBounds.height() / 2f
            canvas.drawText(songName, textX, textY, paint)
        }

        // Album name
        Paint(originalPaint).also { paint ->
            paint.adjustTextSize(albumName, desiredAlbumNameTextWidth, 32f)
            val textBounds = Rect()
            paint.getTextBounds(albumName, 0, albumName.length, textBounds)
            val textX = (posterWidth - textBounds.width()) / 2f
            val textY = artDesiredHeight + posterMargin * 1.68f + textBounds.height() / 2f
            canvas.drawText(albumName, textX, textY, paint)
        }

        // App brand icon
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)?.also { drawable ->
            val iconBitmap = BitmapUtil.getBitmap(drawable, iconSize, iconSize)
            val roundedIconBitmap = BitmapUtil.createRoundedBitmap(iconBitmap, iconSize / 2f)
            if (iconBitmap != roundedIconBitmap){
                iconBitmap.recycle()
            }
            canvas.drawBitmap(roundedIconBitmap, iconMarginLeft, iconMarginTop, null)
            roundedIconBitmap.recycle()
        }

        // App brand name
        Paint(originalPaint).also { paint ->
            paint.letterSpacing = 0.25f
            paint.adjustTextSize(appName, desiredAppNameTextWidth, 48f)
            val textBounds = Rect()
            paint.getTextBounds(appName, 0, appName.length, textBounds)
            val textX = iconMarginLeft + iconSize + 10 + max(0f, desiredAppNameTextWidth - textBounds.width()) / 2
            val textY = iconMarginTop + iconSize / 2 + textBounds.height() / 2
            canvas.drawText(appName, textX, textY, paint)
        }

        return poster
    }

    /**
     * Retrieves the art for the album with id [albumId].
     * NOTE: the method is blocking, should be called on the background thread.
     */
    private fun blockingGetArt(albumId: Long): Bitmap {
        return try {
            val uri = ContentUris.withAppendedId(URI_ALBUM_ART, albumId)
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: throw NullPointerException()
            pfd.use {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                // here, only the options will be resolved
                BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, options)

                val optimalOptions = getOptimalOptions(options)

                // now we can decode the bitmap
                BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, optimalOptions)
            }
        } catch (e: Throwable) {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, R.drawable.art, options)

            val optimalOptions = getOptimalOptions(options)

            BitmapFactory.decodeResource(context.resources, R.drawable.art, optimalOptions)
        }
    }

    /**
     * Calculates the optimal options for [BitmapFactory] targeting [TARGET_WIDTH] and [TARGET_HEIGHT].
     * Actually, it simply calculates the scale factor (@see [BitmapFactory.Options.inSampleSize]),
     * so the width will not exceed [TARGET_WIDTH] and the height will not exceed [TARGET_HEIGHT].
     */
    private fun getOptimalOptions(original: BitmapFactory.Options): BitmapFactory.Options {
        var widthTmp = original.outWidth
        var heightTmp = original.outHeight
        var scale = 1
        while (widthTmp > TARGET_WIDTH || heightTmp > TARGET_HEIGHT) {
            widthTmp /= 2
            heightTmp /= 2
            scale *= 2
        }
        return BitmapFactory.Options().apply { inSampleSize = scale }
    }

    /**
     * Adjusts [this] Paint's text size to make [text] fit [targetWidth].
     * The result text size will not exceed [maxTextSize] limit.
     * The bpdy of the method copied from https://stackoverflow.com/a/7875656/9437681.
     */
    private fun Paint.adjustTextSize(text: String, targetWidth: Float, maxTextSize: Float) {
        if (targetWidth <= 0) return

        var hi = 100f
        var lo = 2f
        val threshold = 0.5f // How close we have to be

        val testPaint = Paint(this)

        while (hi - lo > threshold) {
            val size = (hi + lo) / 2
            testPaint.textSize = size
            if (testPaint.measureText(text) >= targetWidth) hi = size // too big
            else lo = size // too small
        }

        // Use lo so that we undershoot rather than overshoot
        // Use lo so that we undershoot rather than overshoot
        textSize = min(lo, maxTextSize)
    }

    /**
     * Applies blur effect to the given [src].
     */
    private fun applyBlurEffect(src: Bitmap, blurFactor: Int): Bitmap? {
        return try {
            RSBlur.blur(context, src, blurFactor)
        } catch (e: RSRuntimeException) {
            FastBlur.blur(src, blurFactor, true)
        }
    }

    private companion object {
        val URI_ALBUM_ART: Uri = Uri.parse("content://media/external/audio/albumart")

        const val TARGET_WIDTH = 1024
        const val TARGET_HEIGHT = 1024
    }

}