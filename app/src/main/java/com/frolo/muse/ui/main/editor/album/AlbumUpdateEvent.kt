package com.frolo.muse.ui.main.editor.album

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.frolo.music.model.Album


class AlbumUpdateEvent private constructor(
        private val onUpdated: (previous: Album, updated: Album) -> Unit
): BroadcastReceiver() {

    companion object {
        private const val INTENT_ACTION = "com.frolo.muse.ui.main.editor.album.ALBUM_UPDATED"

        private const val ARG_PREVIOUS = "previous"
        private const val ARG_UPDATED = "updated"

        fun dispatch(context: Context, previous: Album, updated: Album) {
            val intent = Intent(INTENT_ACTION)
                    .putExtra(ARG_PREVIOUS, previous)
                    .putExtra(ARG_UPDATED, updated)
            LocalBroadcastManager
                    .getInstance(context)
                    .sendBroadcast(intent)
        }

        fun register(context: Context, onUpdated: (previous: Album, updated: Album) -> Unit): AlbumUpdateEvent {
            return AlbumUpdateEvent(onUpdated).also { receiver ->
                LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(receiver, IntentFilter(INTENT_ACTION))
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == INTENT_ACTION) {
            val previous = intent.getSerializableExtra(ARG_PREVIOUS) as Album
            val updated = intent.getSerializableExtra(ARG_UPDATED) as Album
            onUpdated(previous, updated)
        }
    }

    fun unregister(context: Context) {
        LocalBroadcastManager
                .getInstance(context)
                .unregisterReceiver(this)
    }
}