package com.frolo.muse.engine.service

interface PlayerNotificationSender {
    fun sendPlayerNotification(params: PlayerNotificationParams, forced: Boolean)
}