package com.frolo.muse.repository

import io.reactivex.Single


interface FirebaseRemoteRepository {
    fun getMessagingToken(): Single<String>
}