package com.frolo.muse.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.frolo.muse.AlbumArtUpdateEvent
import com.frolo.muse.arch.SingleLiveEvent


/**
 * This represents a wrapper for [AlbumArtUpdateEvent].
 * This handler respects the lifecycle of the owner to which it is attached.
 * Therefore you don't have to care about detaching it manually: it will do it for you.
 *
 * The handler is backed with a [LiveData] so all the subscribers,
 * that are subscribed with [AlbumArtUpdateHandler.attach] method,
 * will be notified about updates only if they are in [Lifecycle.State.STARTED] state.
 *
 * The handler may be detached manually by calling [detach] method.
 */
class AlbumArtUpdateHandler private constructor(
        private val context: Context,
        private val owner: LifecycleOwner,
        private val onUpdate: (albumId: Long, artData: String?) -> Unit
): LifecycleObserver {

    data class UpdateData
    internal constructor(
            val albumId: Long,
            val artData: String?)

    companion object {

        fun attach(
                fragment: Fragment,
                onUpdate: (albumId: Long, artData: String?) -> Unit
        ): AlbumArtUpdateHandler {
            return AlbumArtUpdateHandler(
                    fragment.requireContext(),
                    fragment,
                    onUpdate)
        }

    }

    private var albumArtUpdateEvent: AlbumArtUpdateEvent? = null

    private val archEvent: SingleLiveEvent<UpdateData> by lazy {
        SingleLiveEvent<UpdateData>().also { event ->
            event.observe(owner, Observer { data ->
                onUpdate(data.albumId, data.artData)
            })
        }
    }

    init {
        owner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    internal fun onAttach() {
        albumArtUpdateEvent = AlbumArtUpdateEvent.register(context) { albumId, artData ->
            archEvent.value = UpdateData(albumId, artData)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detach() {
        albumArtUpdateEvent?.unregister(context)
        owner.lifecycle.removeObserver(this)
    }

}
