package com.frolo.muse.repository

import io.reactivex.Single


interface RemoteConfigRepository {
    fun isLyricsViewerEnabled(): Single<Boolean>
}