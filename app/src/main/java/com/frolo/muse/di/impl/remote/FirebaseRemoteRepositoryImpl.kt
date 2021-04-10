package com.frolo.muse.di.impl.remote

import com.frolo.muse.repository.FirebaseRemoteRepository
import com.frolo.muse.rx.toSingle
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Single


class FirebaseRemoteRepositoryImpl : FirebaseRemoteRepository {

    override fun getMessagingToken(): Single<String> {
        return FirebaseMessaging.getInstance().token.toSingle()
    }

}