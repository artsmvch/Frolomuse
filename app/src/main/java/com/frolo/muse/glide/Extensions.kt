package com.frolo.muse.glide

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.bumptech.glide.RequestManager


fun GlideAlbumArtHelper.observe(owner: LifecycleOwner, observer: (albumId: Long) -> Unit) {
    observe(owner, Observer(observer))
}

fun RequestManager.makeRequest(albumId: Long) =
    GlideAlbumArtHelper.get().makeRequest(this, albumId)