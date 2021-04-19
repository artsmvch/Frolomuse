package com.frolo.muse.di.impl.remote

import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
import com.frolo.muse.repository.RemoteConfigRepository
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Single


class FirebaseRemoteConfigRepositoryImpl : RemoteConfigRepository {

    override fun isLyricsViewerEnabled(): Single<Boolean> {
        return FirebaseRemoteConfigUtil.getActivatedConfig()
            .map { config ->
                config[FirebaseRemoteConfigUtil.LYRICS_VIEWER_ENABLED].let { value ->
                    try {
                        value.asString() == "true"
                    } catch (ignored: Throwable) {
                        false
                    }
                }
            }
    }

}