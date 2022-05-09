package com.frolo.muse.di.impl.local

import android.content.Context
import android.content.SharedPreferences
import com.frolo.muse.repository.Preferences
import com.frolo.muse.repository.RatingPreferences
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable


internal class RatingPreferencesImpl(
    private val context: Context,
    private val preferences: Preferences
) : RatingPreferences {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun isRated(): Flowable<Boolean> {
        val legacyValue = preferences
            .runCatching { rated }
            .getOrElse { true }
        return RxPreference.ofBoolean(prefs, KEY_IS_RATED)
            .get(legacyValue)
    }

    override fun setRated(flag: Boolean): Completable {
        val legacySource = Completable.fromAction {
            preferences.rated = flag
        }
        return RxPreference.ofBoolean(prefs, KEY_IS_RATED)
            .set(flag)
            .mergeWith(legacySource)
    }

    override fun getMinLaunchCountForRatingRequest(): Flowable<Int> {
        val legacyValue = preferences
            .runCatching { minLaunchCountForRatingRequest }
            .getOrElse { DEFAULT_LAUNCH_COUNT_FOR_RATING_REQUEST }
        return RxPreference.ofInt(prefs, KEY_MIN_LAUNCH_COUNT_FOR_RATING_REQUEST)
            .get(legacyValue)
    }

    override fun setMinLaunchCountForRatingRequest(count: Int): Completable {
        val legacySource = Completable.fromAction {
            preferences.minLaunchCountForRatingRequest = count
        }
        return RxPreference.ofInt(prefs, KEY_MIN_LAUNCH_COUNT_FOR_RATING_REQUEST)
            .set(count)
            .mergeWith(legacySource)
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.RATING_PREFERENCES"

        private const val KEY_IS_RATED = "is_rated"
        private const val KEY_MIN_LAUNCH_COUNT_FOR_RATING_REQUEST =
            "min_launch_count_for_rating_request"

        private const val DEFAULT_LAUNCH_COUNT_FOR_RATING_REQUEST = 5
    }
}