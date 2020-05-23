package com.frolo.muse.logger

import com.frolo.muse.repository.Preferences

/**
 * Convenient methods for logging main events in the App.
 */

fun EventLogger.logAppLaunched(launchCount: Int) {
    val params = mapOf("launch_count" to launchCount.toString())
    log("app_launched", params)
}

fun EventLogger.logThemeChanged(@Preferences.Theme theme: Int) {
    val paramValue = when (theme) {
        Preferences.THEME_DARK_BLUE ->          "dark_blue"
        Preferences.THEME_DARK_BLUE_ESPECIAL -> "dark_blue_especial"
        Preferences.THEME_DARK_PURPLE ->        "dark_purple"
        Preferences.THEME_DARK_YELLOW ->        "dark_yellow"
        Preferences.THEME_LIGHT_BLUE ->         "light_blue"
        else ->                                 "unknown"
    }

    val params = mapOf("theme_value" to paramValue)

    log("theme_changed", params)
}

fun EventLogger.logCustomPresetSaved() {
    log("custom_preset_saved")
}

fun EventLogger.logCustomPresetDeleted() {
    log("custom_preset_deleted")
}

fun EventLogger.logMinAudioFileDurationSet(durationInSeconds: Int) {
    val params = mapOf("duration_in_seconds" to durationInSeconds.toString())
    log("min_audio_file_duration_set", params)
}

// TODO: add other main events