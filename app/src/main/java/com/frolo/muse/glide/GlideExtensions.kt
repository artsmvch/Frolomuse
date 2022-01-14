package com.frolo.muse.glide

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.bumptech.glide.RequestManager


private const val NO_ID = -1L

fun GlideAlbumArtHelper.observe(owner: LifecycleOwner, observer: (albumId: Long) -> Unit) {
    observe(owner, Observer(observer))
}

fun RequestManager.makeAlbumArtRequest(albumId: Long?) =
    GlideAlbumArtHelper.get().makeRequest(this, albumId ?: NO_ID)

fun RequestManager.makeAlbumArtRequestAsBitmap(albumId: Long?) =
    GlideAlbumArtHelper.get().makeRequestAsBitmap(this, albumId ?: NO_ID)