package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import com.frolo.muse.model.appearance.ThumbnailConfig
import com.frolo.muse.repository.AppearancePreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable


class AppearancePreferencesImpl(
    private val context: Context
) : AppearancePreferences {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun getThumbnailConfig(): Flowable<ThumbnailConfig> {
        return Flowable.error(UnsupportedOperationException())
    }

    override fun setThumbnailConfig(config: ThumbnailConfig): Completable {
        return Completable.error(UnsupportedOperationException())
    }

    override fun isSnowfallEnabled(): Flowable<Boolean> {
        return RxPreference.ofBoolean(prefs, KEY_SNOWFALL_ENABLED).get(false)
    }

    override fun setSnowfallEnabled(enabled: Boolean): Completable {
        return RxPreference.ofBoolean(prefs, KEY_SNOWFALL_ENABLED).set(enabled)
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.Appearance"

        private const val KEY_SNOWFALL_ENABLED = "snowfall_enabled"
    }

}