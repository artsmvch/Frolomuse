package com.frolo.muse.di.modules

import android.app.Service
import android.support.v4.media.session.MediaSessionCompat
import com.frolo.muse.di.ServiceScope
import com.frolo.muse.player.service.PlayerNotificationSender
import com.frolo.muse.player.service.setEmptyMetadata
import dagger.Module
import dagger.Provides


@Module
class ServiceModule constructor(
    private val service: Service,
    private val notificationSender: PlayerNotificationSender
) {

    @Provides
    fun provideService(): Service = service

    @Provides
    fun providePlayerNotificationSender(): PlayerNotificationSender = notificationSender

    @ServiceScope
    @Provides
    fun provideMediaSession(): MediaSessionCompat {
        return MediaSessionCompat(service, MEDIA_SESSION_TAG).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
            isActive = true
            setEmptyMetadata()
        }
    }

    private companion object {
        private const val MEDIA_SESSION_TAG = "frolomuse:player_service"
    }

}