package com.frolo.muse.ui.base


/**
 * UI components should implement this if they can set padding with no clipping of the content.
 */
interface NoClipping {

    fun removeClipping(left: Int, top: Int, right: Int, bottom: Int)

}