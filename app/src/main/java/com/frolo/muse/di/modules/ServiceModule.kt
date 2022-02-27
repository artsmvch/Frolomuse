package com.frolo.muse.di.modules

import android.app.Service
import android.support.v4.media.session.MediaSessionCompat
import com.frolo.muse.engine.service.PlayerNotificationSender
import dagger.Module
import dagger.Provides


@Module
class ServiceModule constructor(
    private val service: Service,
    private val mediaSession: MediaSessionCompat,
    private val notificationSender: PlayerNotificationSender
) {

    @Provides
    fun provideService(): Service = service

    @Provides
    fun providePlayerNotificationSender(): PlayerNotificationSender = notificationSender

    @Provides
    fun provideMediaSession(): MediaSessionCompat = mediaSession

}