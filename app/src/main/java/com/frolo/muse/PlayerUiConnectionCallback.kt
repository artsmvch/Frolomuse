package com.frolo.muse

import android.app.Activity
import com.frolo.player.Player


interface PlayerUiConnectionCallback {
    fun onPlayerConnectedToUi(uiComponent: Activity, player: Player)
    fun onPlayerDisconnectedFromUi(uiComponent: Activity, player: Player)
}