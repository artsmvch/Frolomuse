package com.frolo.muse.links


interface LinksProcessor {
    fun processLink(link: String, callback: Callback = EMPTY_CALLBACK)

    interface Callback {
        fun onSuccess()
        fun onFailed(error: Throwable)
    }

    companion object {
        val EMPTY_CALLBACK = object : Callback {
            override fun onSuccess() = Unit
            override fun onFailed(error: Throwable) = Unit
        }
    }
}