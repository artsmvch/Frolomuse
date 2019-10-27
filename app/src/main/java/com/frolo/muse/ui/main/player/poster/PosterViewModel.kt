package com.frolo.muse.ui.main.player.poster

import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.renderscript.RSRuntimeException
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.App
import com.frolo.muse.R
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseAndroidViewModel
import io.reactivex.Single
import jp.wasabeef.glide.transformations.internal.FastBlur
import jp.wasabeef.glide.transformations.internal.RSBlur
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min


class PosterViewModel constructor(
        app: App,
        private val schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        private val eventLogger: EventLogger,
        private val songArg: Song
): BaseAndroidViewModel(app, eventLogger) {

    companion object {
        private const val PATH_ALBUM_ART = "content://media/external/audio/albumart"
        private val URI_ALBUM_ART = Uri.parse(PATH_ALBUM_ART)
    }

    private val _posterFile: MutableLiveData<File> = MutableLiveData()

    private val _poster: MutableLiveData<Bitmap> = MutableLiveData()
    val poster: LiveData<Bitmap> = _poster

    private val _isCreatingPoster: MutableLiveData<Boolean> = MutableLiveData()
    val isCreatingPoster: LiveData<Boolean> = _isCreatingPoster

    private val _startedSharingEvent: MutableLiveData<Boolean> = MutableLiveData()
    val startedSharingEvent: LiveData<Boolean> = _startedSharingEvent

    init {
        Single.fromCallable { createPosterBitmapInternal(songArg) }
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isCreatingPoster.value = true }
                .doFinally { _isCreatingPoster.value = false }
                .subscribeFor { bmp -> _poster.value = bmp }
    }

    fun onShareButtonClicked() {
        val bmp = _poster.value ?: return
        val file = _posterFile.value
        if (file != null) {
            // The file exists already no need to create it again
            navigator.sharePoster(songArg, file)
        } else {
            Single.fromCallable { saveBitmapToFileInternal(bmp) }
                    .subscribeOn(schedulerProvider.worker())
                    .observeOn(schedulerProvider.main())
                    .doOnSuccess { posterFile -> _posterFile.value = posterFile }
                    .subscribeFor { posterFile -> navigator.sharePoster(songArg, posterFile) }
        }
    }

    private fun saveBitmapToFileInternal(bmp: Bitmap): File {
        val context: Context = getApplication()
        val fileName = "poster_" + System.currentTimeMillis() + ".png"
        val file = File(context.cacheDir, fileName)
        val fOut = FileOutputStream(file)
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        bmp.recycle()
        fOut.flush()
        fOut.close()
        file.setReadable(true, true)
        return file
    }

    @Throws(java.lang.Exception::class)
    private fun getArtInPreferredSize(retriever: (options: BitmapFactory.Options) -> Bitmap?): Bitmap? {
        // Decode image size
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        retriever(options)
        // The new size we want to scale to
        val preferredSize = 1024
        // Find the correct scale value. It should be the power of 2.
        var widthTmp = options.outWidth
        var heightTmp = options.outHeight
        var scale = 1
        while (true) {
            if (widthTmp < preferredSize && heightTmp < preferredSize)
                break
            widthTmp /= 2
            heightTmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val preferredOptions = BitmapFactory.Options()
        preferredOptions.inSampleSize = scale
        return retriever(preferredOptions)
    }

    @Throws(java.lang.Exception::class)
    private fun Context.getAlbumArt(albumId: Long): Bitmap? {
        val uri = ContentUris.withAppendedId(URI_ALBUM_ART, albumId)
        val pfd = contentResolver.openFileDescriptor(uri, "r") ?: return null
        val fd = pfd.fileDescriptor

        val retriever: (options: BitmapFactory.Options) -> Bitmap? = { options ->
            BitmapFactory.decodeFileDescriptor(fd, null, options)
        }

        return getArtInPreferredSize(retriever)
    }

    @Throws(java.lang.Exception::class)
    private fun Context.getDefaultAlbumArt(): Bitmap {
        val retriever: (options: BitmapFactory.Options) -> Bitmap? = { options ->
            BitmapFactory.decodeResource(resources, R.drawable.art, options)
        }

        return getArtInPreferredSize(retriever) ?: BitmapFactory.decodeResource(resources, R.drawable.art)
    }

    @Throws(java.lang.Exception::class)
    private fun createPosterBitmapInternal(song: Song): Bitmap {
        val context = getApplication<App>()
        val res = context.resources

        val albumId = song.albumId
        val appName = res.getString(R.string.app_name)
        val songName = song.title
        val album = song.album

        val art = try {
            context.getAlbumArt(albumId)
        } catch (e: Throwable) {
            null
        } ?: context.getDefaultAlbumArt()

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

        val poster = applyBlurEffect(context, src, 25)

        //Bitmap blank = Bitmap.createBitmap(artDesiredWidth, artDesiredHeight, Bitmap.Config.ARGB_8888);

        // the poster bmp as the base of canvas
        val canvas = Canvas(poster!!)

        val overlayColor = ContextCompat.getColor(context, R.color.transparent_black)
        canvas.drawColor(overlayColor)

        canvas.drawBitmap(scaledSrc, posterMargin.toFloat(), posterMargin.toFloat(), paint)
        scaledSrc.recycle()

        adjustPaintToDrawText(paint, songName, desiredTitleTextWidth, 57f)
        val textBounds = Rect()
        paint.getTextBounds(songName, 0, Math.max(songName.length - 1, 0), textBounds)
        val txtX = (posterWidth - textBounds.width()) / 2 - 15
        val txtY = (artDesiredHeight.toDouble() + posterMargin * 1.27 + (textBounds.height() / 2).toDouble()).toInt()
        canvas.drawText(songName,
                txtX.toFloat(),
                txtY.toFloat(),
                paint)

        adjustPaintToDrawText(paint, album, desiredAlbumTextWidth, 43f)
        val albumTextBounds = Rect()
        paint.getTextBounds(album, 0, album.length - 1, albumTextBounds)
        val albumTxtX = (posterWidth - albumTextBounds.width()) / 2 - 15
        val albumTxtY = (artDesiredHeight.toDouble() + posterMargin * 1.63 + (textBounds.height() / 2).toDouble()).toInt()
        canvas.drawText(album,
                albumTxtX.toFloat(),
                albumTxtY.toFloat(),
                paint)

        ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)?.let {
            it.setBounds(0, 0, iconSize, iconSize)
            canvas.translate(283f, 35f)
            it.draw(canvas)
            canvas.translate(-283f, -35f)
        }

        paint.textSize = 55f
        canvas.drawText(appName, 385.3f, 95.5f, paint)

        return poster
    }

    private fun adjustPaintToDrawText(paint: Paint, text: String, maxWidth: Float, maxTextSize: Float) {
        val bounds = Rect()
        // stub size
        val testTextSize = 48f
        paint.textSize = testTextSize
        paint.getTextBounds(text, 0, text.length, bounds)
        val desiredTextSize = testTextSize * maxWidth / bounds.width()
        paint.textSize = min(desiredTextSize, maxTextSize)
    }

    private fun applyBlurEffect(context: Context, src: Bitmap, blurFactor: Int): Bitmap? {
        return try {
            RSBlur.blur(context, src, blurFactor)
        } catch (e: RSRuntimeException) {
            FastBlur.blur(src, blurFactor, true)
        }
    }

}