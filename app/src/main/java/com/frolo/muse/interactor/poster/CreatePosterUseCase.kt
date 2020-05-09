package com.frolo.muse.interactor.poster

import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.renderscript.RSRuntimeException
import androidx.core.content.ContextCompat
import com.frolo.muse.R
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.util.BitmapUtil
import io.reactivex.Single
import jp.wasabeef.glide.transformations.internal.FastBlur
import jp.wasabeef.glide.transformations.internal.RSBlur
import javax.inject.Inject
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
        val songName = song.title
        val album = song.album

        val art = blockingGetArt(albumId)

        // preparing size for poster and its elements
        val iconSize = 80
        val posterMargin = 150 // and space for Title and Artist
        val artDesiredWidth = 650
        val artDesiredHeight = 650
        val posterWidth = artDesiredWidth + 2 * posterMargin
        val posterHeight = artDesiredHeight + 2 * posterMargin
        val desiredTitleTextWidth = 800f
        val desiredAlbumTextWidth = 600f

        val bmpWidth = art.width - 1
        val bmpHeight = art.height - 1
        val bmpSize = Math.min(bmpWidth, bmpHeight)
        val x = (bmpWidth - bmpSize) / 2
        val y = (bmpHeight - bmpSize) / 2
        val cropped = Bitmap.createBitmap(art, x, y, bmpSize, bmpSize)
        val src = Bitmap.createScaledBitmap(cropped, posterWidth, posterHeight, true)
        val scaledSrc = Bitmap.createScaledBitmap(cropped, artDesiredWidth, artDesiredHeight, true)
        art.recycle()
        cropped.recycle()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        paint.color = Color.WHITE
        paint.strokeWidth = 5f

        val poster = applyBlurEffect(src, 25)

        //Bitmap blank = Bitmap.createBitmap(artDesiredWidth, artDesiredHeight, Bitmap.Config.ARGB_8888);

        // the poster bmp as the base of canvas
        val canvas = Canvas(poster!!)

        val overlayColor = ContextCompat.getColor(context, R.color.transparent_black)
        canvas.drawColor(overlayColor)

        canvas.drawBitmap(scaledSrc, posterMargin.toFloat(), posterMargin.toFloat(), paint)
        scaledSrc.recycle()

        paint.adjustTextSize(songName, desiredTitleTextWidth, 57f)
        val textBounds = Rect()
        paint.getTextBounds(songName, 0, Math.max(songName.length - 1, 0), textBounds)
        val txtX = (posterWidth - textBounds.width()) / 2 - 15
        val txtY = (artDesiredHeight.toDouble() + posterMargin * 1.27 + (textBounds.height() / 2).toDouble()).toInt()
        canvas.drawText(songName,
                txtX.toFloat(),
                txtY.toFloat(),
                paint)

        paint.adjustTextSize(album, desiredAlbumTextWidth, 43f)
        val albumTextBounds = Rect()
        paint.getTextBounds(album, 0, album.length - 1, albumTextBounds)
        val albumTxtX = (posterWidth - albumTextBounds.width()) / 2 - 15
        val albumTxtY = (artDesiredHeight.toDouble() + posterMargin * 1.63 + (textBounds.height() / 2).toDouble()).toInt()
        canvas.drawText(album,
                albumTxtX.toFloat(),
                albumTxtY.toFloat(),
                paint)

        ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)?.let { d ->
            val iconBitmap = BitmapUtil.getBitmap(d, iconSize, iconSize)
            val roundedIconBitmap = BitmapUtil.createRoundedBitmap(iconBitmap, iconSize / 2f)
            if (iconBitmap != roundedIconBitmap){
                iconBitmap.recycle()
            }
            val roundedIconDrawable = BitmapDrawable(context.resources, roundedIconBitmap)

            roundedIconDrawable.setBounds(0, 0, iconSize, iconSize)
            canvas.translate(283f, 35f)
            roundedIconDrawable.draw(canvas)
            canvas.translate(-283f, -35f)
        }

        paint.textSize = 55f
        canvas.drawText(appName, 385.3f, 95.5f, paint)

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
     */
    private fun Paint.adjustTextSize(
        text: String,
        targetWidth: Float,
        maxTextSize: Float
    ) {
        val bounds = Rect()
        // stub size
        val testTextSize = 48f
        textSize = testTextSize
        getTextBounds(text, 0, text.length, bounds)
        val desiredTextSize = testTextSize * targetWidth / bounds.width()
        textSize = min(desiredTextSize, maxTextSize)
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