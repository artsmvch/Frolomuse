package com.frolo.muse.audiofx2

import android.content.Context
import androidx.core.content.edit
import com.frolo.audiofx.AudioFxImpl
import com.frolo.audiofx2.AudioFx2
import com.frolo.muse.di.impl.local.PresetRepositoryImpl
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

/**
 * Migrates AudioFx data to AudioFx2.
 */
internal class AudioFx2Migrator(
    private val context: Context,
    private val audioFx2: AudioFx2
) {
    fun migrate() = kotlin.runCatching {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_MIGRATED, false)) {
            migrateActual()
            prefs.edit { putBoolean(KEY_MIGRATED, true) }
        }
    }

    private fun migrateActual() {
        migrateCommon()
        migrateEqualizerPresets()
    }

    private fun migrateCommon() {
        Completable.fromAction {
            val prefsName = "com.frolo.muse.audiofx.persistence"
            val audioFx = AudioFxImpl.getInstance(context, prefsName)
            if (audioFx.isEnabled) {
                // Copy the enabled status
                audioFx2.equalizer?.isEnabled = true
                audioFx2.bassBoost?.isEnabled = true
                audioFx2.virtualizer?.isEnabled = true
                audioFx2.reverb?.isEnabled = true
            }
            // Copy the bass strength
            audioFx2.bassBoost?.value = audioFx.bassStrength.toInt()
            // Copy the virtualizer strength
            audioFx2.virtualizer?.value = audioFx.virtualizerStrength.toInt()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    private fun migrateEqualizerPresets() {
        val equalizer = audioFx2.equalizer ?: return
        // The repo should be injected, but anyway this code will be removed soon
        val presetRepository = PresetRepositoryImpl(context)
        presetRepository.presets
            .firstOrError()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMapCompletable { presets ->
                val sources = presets.map { preset ->
                    val source = Completable.fromAction {
                        // Copy the preset to the new AudioFx2
                        val levels = HashMap<Int, Int>(preset.levelCount)
                        for (band in 0 until preset.levelCount) {
                            levels[band] = levels[band]!!
                        }
                        equalizer.createPreset(preset.name, levels)
                    }
                    source
                        .subscribeOn(Schedulers.io())
                         // Delete the preset from the old repo
                        .andThen(presetRepository.delete(preset))
                }
                Completable.merge(sources)
            }
            .subscribe()
    }

    companion object {
        private const val PREFS_NAME = "com.frolo.muse.audiofx2.migration"

        private const val KEY_MIGRATED = "migrated"
    }
}