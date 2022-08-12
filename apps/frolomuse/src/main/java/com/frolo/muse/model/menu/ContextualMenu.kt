package com.frolo.muse.model.menu

import com.frolo.music.model.Media


data class ContextualMenu<E : Media> constructor(
    val targetItem: E,
    val selectAllOptionAvailable: Boolean,
    val playOptionAvailable: Boolean,
    val playNextOptionAvailable: Boolean,
    val addToQueueOptionAvailable: Boolean,
    val deleteOptionAvailable: Boolean,
    val shareOptionAvailable: Boolean,
    val addToPlaylistOptionAvailable: Boolean,
    val hideOptionAvailable: Boolean,
    val scanFilesOptionAvailable: Boolean
)
