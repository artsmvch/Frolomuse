package com.frolo.muse.repository

import com.frolo.muse.model.appearance.ThumbnailConfig
import io.reactivex.Completable
import io.reactivex.Flowable


interface AppearancePreferences {
    fun getThumbnailConfig(): Flowable<ThumbnailConfig>
    fun setThumbnailConfig(config: ThumbnailConfig): Completable
}