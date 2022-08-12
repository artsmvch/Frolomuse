package com.frolo.muse.model.event

import com.frolo.music.model.Media


/**
 * Confirmation of multiple deletions of [mediaItems] which are associated with [associatedMediaItem].
 */
data class MultipleDeletionConfirmation<E: Media>(
    val mediaItems: List<E>,
    val associatedMediaItem: Media?
)