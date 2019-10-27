package com.frolo.muse

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

import com.frolo.muse.arch.SingleLiveEvent


@Deprecated(
        message = "No code currently is dispatching events to this center",
        replaceWith = ReplaceWith("AlbumArtUpdateEvent.dispatch"),
        level = DeprecationLevel.HIDDEN)
object AlbumArtUpdateDispatcher {

    private val EVENT = SingleLiveEvent<UpdateData>()

    data class UpdateData
    internal constructor(
            val albumId: Long,
            val artData: String?)

    @MainThread
    fun dispatch(
            albumId: Long,
            artData: String?) {
        val updateData = UpdateData(albumId, artData)
        EVENT.value = updateData
    }

    @MainThread
    fun observe(
            owner: LifecycleOwner,
            observer: Observer<UpdateData>) {
        EVENT.observe(owner, observer)
    }

    @MainThread
    fun observe(
            owner: LifecycleOwner,
            onUpdate: (albumId: Long, artData: String?) -> Unit) {
        EVENT.observe(owner, Observer { data ->
            onUpdate(data.albumId, data.artData)
        })
    }

}
