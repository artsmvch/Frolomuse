package com.frolo.muse.ui.main.player


/**
 * UI components that have a [PlayerFragment] in it should implement this interface to obey its method calls.
 */
interface PlayerFragCallback {

    fun setPlayerSheetDraggable(draggable: Boolean)

}