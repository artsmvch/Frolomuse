package com.frolo.muse.repository

import io.reactivex.Completable
import io.reactivex.Flowable


interface FirebasePreferences {
    fun getMessagingToken(): Flowable<String>
    fun setMessagingToken(token: String): Completable
}