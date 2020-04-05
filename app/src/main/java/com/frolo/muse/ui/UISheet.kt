package com.frolo.muse.ui


/**
 * UI components that represent some kinds of sheet should implement this interface to handle slide offsets.
 */
interface UISheet {

    fun onSheetSlide(offset: Float)

}