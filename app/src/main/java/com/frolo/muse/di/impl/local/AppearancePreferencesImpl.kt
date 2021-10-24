package com.frolo.muse.di.impl.local

import android.content.Context
import com.frolo.muse.model.appearance.ThumbnailConfig
import com.frolo.muse.repository.AppearancePreferences
import io.reactivex.Completable
import io.reactivex.Flowable


class AppearancePreferencesImpl(
    private val context: Context
) : AppearancePreferences {

    override fun getThumbnailConfig(): Flowable<ThumbnailConfig> {
        return Flowable.error(UnsupportedOperationException())
    }

    override fun setThumbnailConfig(config: ThumbnailConfig): Completable {
        return Completable.error(UnsupportedOperationException())
    }

}