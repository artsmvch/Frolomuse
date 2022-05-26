package com.frolo.muse.engine

import com.frolo.player.Player
import io.reactivex.Observable


interface PlayerHolder {
    fun peekPlayer(): Player?
    fun getPlayerAsync(): Observable<Player>
}