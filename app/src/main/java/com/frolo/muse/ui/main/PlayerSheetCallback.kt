package com.frolo.muse.ui.main


/**
 * UI component that has a [PlayerSheetFragment] in it should implement this interface to properly handle its state.
 */
interface PlayerSheetCallback {

    fun setPlayerSheetDraggable(draggable: Boolean)

    fun requestCollapse()

}