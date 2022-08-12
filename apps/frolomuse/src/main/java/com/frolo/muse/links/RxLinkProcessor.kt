package com.frolo.muse.links

import io.reactivex.Completable


fun LinksProcessor.processLinkAsync(link: String): Completable {
    return Completable.create { emitter ->
        val callback = object : LinksProcessor.Callback {
            override fun onSuccess() {
                emitter.onComplete()
            }
            override fun onFailed(error: Throwable) {
                emitter.onError(error)
            }
        }
        processLink(link, callback)
    }
}