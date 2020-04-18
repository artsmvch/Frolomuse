package com.frolo.muse.glide

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


fun GlideAlbumArtHelper.observe(owner: LifecycleOwner, observer: (albumId: Long) -> Unit) {
    observe(owner, Observer(observer))
}

fun RequestManager.makeRequest(albumId: Long) =
    GlideAlbumArtHelper.get().makeRequest(this, albumId)

fun RequestManager.makeRequestAsBitmap(albumId: Long) =
    GlideAlbumArtHelper.get().makeRequestAsBitmap(this, albumId)

fun RequestBuilder<Bitmap>.whenLoadFailed(
        action: (e: GlideException) -> Unit
) = this.addListener(
        object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                if (e != null) action.invoke(e)
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean = false
        }
)

fun RequestBuilder<Bitmap>.whenResourceReady(
        action: (resource: Bitmap) -> Unit
) = this.addListener(
    object : RequestListener<Bitmap> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Bitmap>?,
            isFirstResource: Boolean
        ): Boolean = false

        override fun onResourceReady(
            resource: Bitmap?,
            model: Any?,
            target: Target<Bitmap>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            if (resource != null) action.invoke(resource)
            return false
        }
    }
)