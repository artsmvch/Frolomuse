package com.frolo.muse.links

import com.frolo.muse.router.AppRouter


internal class AppLinksProcessor(
    private val router: AppRouter
): LinksProcessor {
    override fun processLink(link: String, callback: LinksProcessor.Callback) {
        callback.onFailed(UnsupportedOperationException("Not implemented"))
    }
}