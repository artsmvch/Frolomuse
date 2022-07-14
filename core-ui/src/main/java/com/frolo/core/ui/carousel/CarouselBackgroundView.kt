package com.frolo.core.ui.carousel

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.frolo.core.ui.glide.GlideAlbumArtHelper
import com.frolo.debug.DebugUtils
import com.frolo.player.AudioSource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


// TODO: get rid of RxJava and Glide, use custom thread instead
class CarouselBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    @ColorInt
    private var color: Int? = null

    private var colorLoaderDisposable: Disposable? = null
    private var colorAnimator: Animator? = null

    fun loadColorAsync(source: AudioSource?, @ColorInt defColor: Int) {
        colorLoaderDisposable?.dispose()
        colorLoaderDisposable = createLoader(source, defColor).subscribe(
            { color ->
                animateToColor(color)
            },
            { err ->
                DebugUtils.dumpOnMainThread(err)
            }
        )
    }

    private fun createLoader(source: AudioSource?, @ColorInt defColor: Int): Single<Int> {
        val uri: Uri? = if (source != null) {
            GlideAlbumArtHelper.getUri(source.metadata.albumId)
        } else {
            null
        }
        val future = Glide.with(this)
            .asBitmap()
            .load(uri)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .submit(RESOURCE_SIZE, RESOURCE_SIZE)
        return Single.fromFuture(future, Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { bmp -> Palette.from(bmp) }
            .map { palette -> palette.generate() }
            .map { palette ->
                palette.getDarkMutedColor(defColor)
            }
            .onErrorReturnItem(defColor)
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun animateToColor(@ColorInt color: Int) {
        colorAnimator?.cancel()
        colorAnimator = createColorAnimator(
            fromColor = this.color ?: Color.TRANSPARENT,
            toColor = color
        ).apply { start() }
    }

    private fun createColorAnimator(
        @ColorInt fromColor: Int,
        @ColorInt toColor: Int
    ): ValueAnimator {
        return ValueAnimator.ofArgb(fromColor, toColor).apply {
            duration = 300L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                setColorInternal(it.animatedValue as Int)
            }
        }
    }

    private fun setColorInternal(@ColorInt color: Int) {
        this.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        color?.also { safeColor ->
            canvas.drawColor(safeColor)
        }
    }

    private companion object {
        private const val RESOURCE_SIZE = 40
    }

}