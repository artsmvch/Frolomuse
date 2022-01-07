package com.frolo.muse.model.event

import com.frolo.music.model.Media


/**
 * Determines how exactly the user wants to delete one or more media items.
 */
sealed class DeletionType {

    /**
     * Delete only from the associated [media].
     */
    data class FromAssociatedMedia(val media: Media): DeletionType()

    /**
     * Delete completely from the internal memory of the device.
     */
    object FromDevice: DeletionType()

}