package com.frolo.muse.logger

import androidx.annotation.StringDef
import com.frolo.muse.model.Theme
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song

/**
 * Convenient methods for logging main events in the App.
 */

fun EventLogger.logAppLaunched(launchCount: Int) {
    val params = mapOf("launch_count" to launchCount.toString())
    log("app_launched", params)
}

//region Settings

fun EventLogger.logThemeChanged(theme: Theme?) {
    val paramValue = when (theme) {
        Theme.DARK_BLUE ->          "dark_blue"
        Theme.DARK_BLUE_ESPECIAL -> "dark_blue_especial"
        Theme.DARK_PURPLE ->        "dark_purple"
        Theme.DARK_ORANGE ->        "dark_orange"
        Theme.DARK_GREEN ->         "dark_green"
        Theme.LIGHT_BLUE ->         "light_blue"
        Theme.LIGHT_PINK ->         "light_pink"
        null ->                     "null"
    }

    val params = mapOf("theme_value" to paramValue)

    log("theme_changed", params)
}

fun EventLogger.logMinAudioFileDurationSet(durationInSeconds: Int) {
    val params = mapOf("duration_in_seconds" to durationInSeconds.toString())
    log("min_audio_file_duration_set", params)
}

fun EventLogger.logSleepTimerSet(hours: Int, minutes: Int, seconds: Int) {
    val durationInSeconds = hours * 60 * 60 + minutes * 60 + seconds
    val params = mapOf("duration_in_seconds" to durationInSeconds.toString())
    log("sleep_timer_set", params)
}

fun EventLogger.logMediaLibraryScanned() {
    log("media_library_scanned")
}

fun EventLogger.logEasterEggFound(clickCount: Int) {
    val params = mapOf("click_count" to clickCount.toString())
    log("easter_egg_found", params)
}

//endregion

//region Rate App Dialog

@Retention(AnnotationRetention.SOURCE)
@StringDef(RATE_DIALOG_ANSWER_YES, RATE_DIALOG_ANSWER_NO, RATE_DIALOG_ANSWER_REMIND_LATER, RATE_DIALOG_ANSWER_NULL)
annotation class RateDialogAnswer

@RateDialogAnswer const val RATE_DIALOG_ANSWER_YES = "yes"
@RateDialogAnswer const val RATE_DIALOG_ANSWER_NO = "no"
@RateDialogAnswer const val RATE_DIALOG_ANSWER_REMIND_LATER = "remind_later"
@RateDialogAnswer const val RATE_DIALOG_ANSWER_NULL = "NULL"

fun EventLogger.logRateDialogAnswered(@RateDialogAnswer answer: String) {
    val params = mapOf(
        "answer" to answer,
        "cancelled" to false.toString()
    )
    log("rate_dialog", params)
}

fun EventLogger.logRateDialogCancelled() {
    val params = mapOf(
        "answer" to RATE_DIALOG_ANSWER_NULL,
        "cancelled" to true.toString()
    )
    log("rate_dialog", params)
}

//endregion


//region Rate/Share App

fun EventLogger.logAppRatedFromSettings() {
    log("app_rated_from_settings")
}

fun EventLogger.logAppSharedFromSettings() {
    log("app_shared_from_settings")
}

//endregion


//region Song

fun EventLogger.logSongUpdated() {
    log("song_updated")
}

//endregion

//region Album

fun EventLogger.logAlbumUpdated(albumArtDeleted: Boolean) {
    val params = mapOf("album_art_deleted" to albumArtDeleted.toString())
    log("album_updated", params)
}

//endregion


//region Playlist

fun EventLogger.logPlaylistCreated(initialSongCount: Int) {
    val params = mapOf("initial_song_count" to initialSongCount.toString())
    log("playlist_created", params)
}

fun EventLogger.logPlaylistUpdated() {
    log("playlist_updated")
}

fun EventLogger.logSongsAddedToPlaylist(songCount: Int) {
    val params = mapOf("song_count" to songCount.toString())
    log("songs_added_to_playlist", params)
}

fun EventLogger.logMediaAddedToPlaylist(mediaCount: Int) {
    val params = mapOf("media_count" to mediaCount.toString())
    log("media_added_to_playlist", params)
}

//endregion

//region Custom Preset

fun EventLogger.logCustomPresetSaved() {
    log("custom_preset_saved")
}

fun EventLogger.logCustomPresetDeleted() {
    log("custom_preset_deleted")
}

//endregion

//region MyFile

fun EventLogger.logFolderSetAsDefault() {
    log("folder_set_as_default")
}

fun EventLogger.logFilesHidden(fileCount: Int) {
    val params = mapOf("file_count" to fileCount.toString())
    log("files_hidden", params)
}

fun EventLogger.logFilesUnhidden(fileCount: Int) {
    val params = mapOf("file_count" to fileCount.toString())
    log("files_unhidden", params)
}

fun EventLogger.logFilesScanned(fileCount: Int) {
    val params = mapOf("file_count" to fileCount.toString())
    log("files_scanned", params)
}

//endregion


//region Library Sections

fun EventLogger.logLibrarySectionsSaved(changed: Boolean) {
    val params = mapOf("changed" to changed.toString())
    log("library_sections_saved", params)
}

//endregion

//region Shortcuts

fun EventLogger.logShortcutCreated(@Media.Kind kindOfMedia: Int) {
    val kindOfMediaValue = when (kindOfMedia) {
        Media.ALBUM ->      "album"
        Media.ARTIST ->     "artist"
        Media.GENRE ->      "genre"
        Media.MY_FILE ->    "my_file"
        Media.PLAYLIST ->   "playlist"
        Media.SONG ->       "song"
        else ->             "NULL"
    }

    val params = mapOf("kind_of_media" to kindOfMediaValue)
    log("shortcut_created", params)
}

//endregion

//region Poster

fun EventLogger.logPosterShared() {
    log("poster_shared")
}

//endregion


//region Search

fun EventLogger.logMediaSearchUsed(queryCount: Int) {
    val params = mapOf("query_count" to queryCount.toString())
    log("media_search_used", params)
}

//endregion


//region Playback Fading

fun EventLogger.logPlaybackFadingConfigured(seconds: Int) {
    val params = mapOf("duration" to seconds.toString())
    log("playback_fading_configured", params)
}

//endregion


//region Advertising

fun EventLogger.logBannerCanBeShown() {
    log("banner_can_be_shown")
}

//endregion


//region Player Widget

fun EventLogger.logPlayerWidgetEnabled() {
    log("player_widget_enabled")
}

fun EventLogger.logPlayerWidgetDeleted(count: Int) {
    val params = mapOf("count" to count.toString())
    log("player_widget_deleted", params)
}

fun EventLogger.logPlayerWidgetDisabled() {
    log("player_widget_disabled")
}

//endregion


//region Lyrics

fun EventLogger.logLyricsViewed() {
    log("lyrics_viewed")
}

fun EventLogger.logFailedToGetLyrics(song: Song, err: Throwable) {
    val params = mapOf(
        "artist_name" to song.artist,
        "song_name" to song.title,
        "error" to err.toString()
    )
    log("failed_to_get_lyrics", params)
}

fun EventLogger.logLyricsSaved(edited: Boolean) {
    val params = mapOf(
        "edited" to edited.toString()
    )
    log("lyrics_saved", params)
}

//endregion


//region Player screen

fun EventLogger.logPlayerOptionsMenuShown() {
    log("player_options_menu_shown")
}

//endregion