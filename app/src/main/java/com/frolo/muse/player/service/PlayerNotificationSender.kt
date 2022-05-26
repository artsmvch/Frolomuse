package com.frolo.muse.player.service

interface PlayerNotificationSender {
    fun sendPlayerNotification(params: PlayerNotificationParams, forced: Boolean)
}