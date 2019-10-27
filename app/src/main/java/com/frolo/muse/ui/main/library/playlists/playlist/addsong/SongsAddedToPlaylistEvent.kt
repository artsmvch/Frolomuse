package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.frolo.muse.model.media.Playlist


class SongsAddedToPlaylistEvent private constructor(
        private val onSongsAdded: (playlist: Playlist) -> Unit
): BroadcastReceiver() {

    companion object {
        private const val INTENT_ACTION = "com.frolo.muse.ui.main.library.playlists.playlist.addsong.SONGS_ADDED_TO_PLAYLISt"

        private const val ARG_PLAYLIST = "playlist"

        fun dispatch(context: Context, playlist: Playlist) {
            val intent = Intent(INTENT_ACTION)
                    .putExtra(ARG_PLAYLIST, playlist)
            context.sendBroadcast(intent)
        }

        fun register(context: Context, onSongsAdded: (playlist: Playlist) -> Unit): SongsAddedToPlaylistEvent {
            return SongsAddedToPlaylistEvent(onSongsAdded).also { receiver ->
                context.registerReceiver(receiver, IntentFilter(INTENT_ACTION))
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == INTENT_ACTION) {
            val playlist = intent.getSerializableExtra(ARG_PLAYLIST) as Playlist
            onSongsAdded(playlist)
        }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}