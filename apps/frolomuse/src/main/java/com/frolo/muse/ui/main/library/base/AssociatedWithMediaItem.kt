package com.frolo.muse.ui.main.library.base

import com.frolo.music.model.Media


/**
 * Indicates that the object is associated with a media item.
 */
interface AssociatedWithMediaItem {
    val associatedMediaItem: Media?
}

@Suppress("FunctionName")
fun AssociatedWithMediaItem(item: Media): AssociatedWithMediaItem {
    return object : AssociatedWithMediaItem {
        override val associatedMediaItem: Media? get() = item
    }
}