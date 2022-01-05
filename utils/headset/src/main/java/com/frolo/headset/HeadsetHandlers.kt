package com.frolo.headset


/**
 * Convenient method for creating [HeadsetHandler] in Kotlin.
 */
fun createHeadsetHandler(
    onConnected: (() -> Unit)? = null,
    onDisconnected: (() -> Unit)? = null,
    onBecomeWeird: (() -> Unit)? = null
): HeadsetHandler {

    val callback = object : HeadsetHandler.Callback {

        override fun onConnected() {
            onConnected?.invoke()
        }

        override fun onDisconnected() {
            onDisconnected?.invoke()
        }

        override fun onBecomeWeird() {
            onBecomeWeird?.invoke()
        }

    }

    return HeadsetHandler.create(callback)

}