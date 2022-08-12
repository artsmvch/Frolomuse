package com.frolo.muse.ui.main.library

import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * Fragments should implement this interface if they want to have an action button.
 */
interface ActionButtonCallback {
    /**
     * Called when the host checks if this callback currently requires the action button.
     */
    fun requiresActionButton(): Boolean

    /**
     * Called when the host needs this callback to decorate the action button in its own way.
     */
    fun onDecorateActionButton(button: FloatingActionButton)

    /**
     * Called when the host needs this callback to handle the action button click.
     */
    fun onHandleActionButtonClick()
}