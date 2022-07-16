package com.frolo.muse.links

import android.net.Uri
import com.frolo.muse.router.AppRouter
import com.frolo.muse.util.LinkUtils


internal class AppLinksProcessor(
    private val router: AppRouter
): LinksProcessor {
    override fun processLink(link: String, callback: LinksProcessor.Callback) {
        val uri: Uri = try {
            Uri.parse(link)
        } catch (e: Throwable) {
            callback.onFailed(e)
            return
        }
        val scheme: String? = uri.scheme
        if (scheme != null && LinkUtils.isHttpScheme(scheme)) {
            processHttpLinkUri(uri, callback)
        } else {
            callback.onFailed(UnsupportedOperationException("Not implemented"))
        }
    }

    private fun processHttpLinkUri(uri: Uri, callback: LinksProcessor.Callback) {
        when (uri.pathSegments?.getOrNull(0)) {
            "play",
            "player" -> {
                router.openPlayer()
                callback.onSuccess()
            }
            "library" -> {
                router.openLibrary()
                callback.onSuccess()
            }
            "equalizer",
            "audiofx" -> {
                router.openAudioFx()
                callback.onSuccess()
            }
            "search" -> {
                router.openAudioFx()
                callback.onSuccess()
            }
            "settings" -> {
                router.openSettings()
                callback.onSuccess()
            }
            else -> {
                callback.onFailed(IllegalArgumentException("Unknown link: $uri"))
            }
        }
    }
}