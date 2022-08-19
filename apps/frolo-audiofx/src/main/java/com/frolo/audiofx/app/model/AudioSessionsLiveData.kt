package com.frolo.audiofx.app.model

import android.app.Application
import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData


class AudioSessionsLiveData constructor(
    private val application: Application
) : LiveData<List<AudioSession>>() {

    private val mediaSessionManager: MediaSessionManager? by lazy {
        application.getSystemService<MediaSessionManager>()
    }
    private val audioSessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            processAudioSessions(controllers)
        }

    private fun processAudioSessions(controllers: List<MediaController>?) {
        val audioSessionList: List<AudioSession> = controllers.orEmpty().mapNotNull { controller ->
            null
        }
        postValue(audioSessionList)
    }

    override fun onActive() {
        startObservingAudioSessions()
    }

    private fun startObservingAudioSessions() {
        val notificationListener: ComponentName? =
            null //ComponentName(application, NotificationListener::class.java)
        mediaSessionManager?.addOnActiveSessionsChangedListener(
            audioSessionListener, notificationListener)
        mediaSessionManager?.getActiveSessions(notificationListener).also(::processAudioSessions)
    }

    override fun onInactive() {
        stopObservingAudioSessions()
    }

    private fun stopObservingAudioSessions() {
        mediaSessionManager?.removeOnActiveSessionsChangedListener(audioSessionListener)
    }
}