package com.frolo.muse.ui.main.library.playlists.create

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.frolo.muse.model.media.Playlist


class PlaylistCreateEvent private constructor(
        private val onCreated: (playlist: Playlist) -> Unit
): BroadcastReceiver() {

    companion object {
        private const val INTENT_ACTION = "com.frolo.muse.ui.main.editor.song.PLAYLIST_CREATED"

        private const val ARG_PLAYLIST = "playlist"

        fun dispatch(context: Context, playlist: Playlist) {
            val intent = Intent(INTENT_ACTION)
                    .putExtra(ARG_PLAYLIST, playlist)
            LocalBroadcastManager
                    .getInstance(context)
                    .sendBroadcast(intent)
        }

        fun register(context: Context, onCreated: (playlist: Playlist) -> Unit): PlaylistCreateEvent {
            return PlaylistCreateEvent(onCreated).also { receiver ->
                LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(receiver, IntentFilter(INTENT_ACTION))
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == INTENT_ACTION) {
            val playlist = intent.getSerializableExtra(ARG_PLAYLIST) as Playlist
            onCreated(playlist)
        }
    }

    fun unregister(context: Context) {
        LocalBroadcastManager
                .getInstance(context)
                .unregisterReceiver(this)
    }
}