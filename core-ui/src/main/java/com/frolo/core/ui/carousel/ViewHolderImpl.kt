package com.frolo.core.ui.carousel

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.frolo.core.ui.R
import com.frolo.core.ui.glide.makeAlbumArtRequest
import com.frolo.player.AudioSource


internal class ViewHolderImpl(
    val view: View,
    private val imageContainer: View,
    private val imageView: ImageView,
    private val progressBar: View,
    private val requestManager: RequestManager
): RequestListener<Drawable> {

    fun bind(item: AudioSource?) {
        progressBar.visibility = View.VISIBLE
        imageContainer.visibility = View.INVISIBLE
        createArtRequest(item).into(imageView)
    }

    private fun createArtRequest(item: AudioSource?): RequestBuilder<Drawable> {
        // The error drawable is a large PNG,
        // so we need to load it through a split request
        // in order to resize correctly and avoid OOM errors.
        val errorRequest = requestManager
            .load(R.drawable.art_placeholder)
            .skipMemoryCache(false)

        return requestManager.makeAlbumArtRequest(item?.metadata?.albumId)
            // The memory cache is not that important for this adapter,
            // but disabling it here may help avoid OOM errors.
            .skipMemoryCache(true)
            .placeholder(null)
            .error(errorRequest)
            .addListener(this)
            .transition(DrawableTransitionOptions().crossFade(200))
    }

    fun recycle() {
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>,
        isFirstResource: Boolean
    ): Boolean {
        progressBar.visibility = View.INVISIBLE
        imageContainer.visibility = View.VISIBLE
        return false
    }

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>?,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        progressBar.visibility = View.INVISIBLE
        imageContainer.visibility = View.VISIBLE
        return false
    }
}