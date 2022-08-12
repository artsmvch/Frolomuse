package com.frolo.muse.ui.main.audiofx.preset

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.frolo.audiofx.CustomPreset


class PresetSavedEvent private constructor(
        private val onSaved: (preset: CustomPreset) -> Unit
): BroadcastReceiver() {

    companion object {
        private const val INTENT_ACTION = "com.frolo.muse.ui.main.audiofx.preset.PRESET_SAVED"

        private const val ARG_PRESET = "preset"

        fun dispatch(context: Context, preset: CustomPreset) {
            val intent = Intent(INTENT_ACTION)
                    .putExtra(ARG_PRESET, preset)
            LocalBroadcastManager
                    .getInstance(context)
                    .sendBroadcast(intent)
        }

        fun register(context: Context, onSaved: (preset: CustomPreset) -> Unit): PresetSavedEvent {
            return PresetSavedEvent(onSaved).also { receiver ->
                LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(receiver, IntentFilter(INTENT_ACTION))
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == INTENT_ACTION) {
            val preset = intent.getSerializableExtra(ARG_PRESET) as CustomPreset
            onSaved(preset)
        }
    }

    fun unregister(context: Context) {
        LocalBroadcastManager
                .getInstance(context)
                .unregisterReceiver(this)
    }
}