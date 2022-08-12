package com.frolo.muse.repository

import io.reactivex.Completable
import io.reactivex.Flowable


interface RatingPreferences {
    fun isRated(): Flowable<Boolean>
    fun setRated(flag: Boolean): Completable

    fun getMinLaunchCountForRatingRequest(): Flowable<Int>
    fun setMinLaunchCountForRatingRequest(count: Int): Completable
}