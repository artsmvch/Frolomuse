package com.frolo.muse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IntDef
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.frolo.muse.App
import com.frolo.muse.R
import com.frolo.muse.common.albumId
import com.frolo.muse.common.title
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.service.PlayerService
import com.frolo.muse.engine.service.PlayerService.Companion.newIntentFromWidget
import com.frolo.muse.glide.GlideAlbumArtHelper
import com.frolo.muse.ui.main.MainActivity.Companion.newIntent
import java.util.concurrent.TimeUnit

@IntDef(
    RC_COMMAND_TOGGLE,
    RC_COMMAND_SKIP_TO_PREVIOUS,
    RC_COMMAND_SKIP_TO_NEXT,
    RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE,
    RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE,
    RC_OPEN_PLAYER
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.LOCAL_VARIABLE)
private annotation class RequestCode

private const val RC_COMMAND_TOGGLE = 123
private const val RC_COMMAND_SKIP_TO_PREVIOUS = 124
private const val RC_COMMAND_SKIP_TO_NEXT = 125
private const val RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE = 126
private const val RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE = 127
private const val RC_OPEN_PLAYER = 128

@Suppress("FunctionName")
private fun PendingIntent(context: Context, @RequestCode requestCode: Int, intent: Intent, flags: Int): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        PendingIntent.getForegroundService(context, requestCode, intent, flags)
    } else {
        PendingIntent.getService(context, requestCode, intent, flags)
    }
}

fun AppWidgetManager.updatePlayerWidget(
    context: Context,
    player: Player?,
    vararg widgetIds: Int
) {

    val currItem: AudioSource?
    val isPlaying: Boolean
    @Player.RepeatMode val repeatMode: Int
    @Player.ShuffleMode val shuffleMode: Int
    if (player != null && !player.isShutdown()) {
        currItem = player.getCurrent()
        isPlaying = player.isPlaying()
        repeatMode = player.getRepeatMode()
        shuffleMode = player.getShuffleMode()
    } else {
        // There is no active player
        val app = context.applicationContext as? App
        if (app != null) {
            val appComponent = app.appComponent
            val preferences = appComponent.providePreferences()
            val songRepository = appComponent.provideSongRepository()
            currItem = try {
                val lastSongId = preferences.lastSongId
                songRepository.getItem(lastSongId)
                    .firstOrError()
                    // max 1 second to load the song
                    .timeout(1, TimeUnit.SECONDS)
                    .blockingGet()
                    .toAudioSource()
            } catch (ignored: Throwable) {
                null
            }
            isPlaying = false
            repeatMode = preferences.loadRepeatMode()
            shuffleMode = preferences.loadShuffleMode()
        } else {
            // We could not load anything :(
            currItem = null
            isPlaying = false
            repeatMode = Player.REPEAT_OFF
            shuffleMode = Player.SHUFFLE_OFF
        }
    }

    widgetIds.forEach { widgetId ->
        val widgetOptions: Bundle? = this.getAppWidgetOptions(widgetId)

        val minWidth: Int? = widgetOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight: Int? = widgetOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        val remoteViews = PlayerRemoteViews(
            context = context,
            currItemTitle = currItem?.title,
            isPlaying = isPlaying,
            repeatMode = repeatMode,
            shuffleMode = shuffleMode,
            minWidth = minWidth,
            minHeight = minHeight
        )

        this.updateAppWidget(widgetIds, remoteViews)

        if (hasRoomForAlbumArt(minWidth, minHeight)) {
            loadArt(
                context = context,
                albumArtId = currItem?.albumId,
                remoteViews = remoteViews,
                widgetIds = *widgetIds
            )
        }
    }
}

private fun hasRoomForAlbumArt(minWidth: Int?, minHeight: Int?): Boolean {
    val horizontalCells = getCellsForSize(minWidth ?: 0)
    return horizontalCells >= 4
}

private fun hasRoomForRepeatAndShuffleModes(minWidth: Int?, minHeight: Int?): Boolean {
    val horizontalCells = getCellsForSize(minWidth ?: 0)
    return horizontalCells >= 3
}

@Suppress("FunctionName")
fun PlayerRemoteViews(
    context: Context,
    currItemTitle: String?,
    isPlaying: Boolean,
    @Player.RepeatMode repeatMode: Int,
    @Player.ShuffleMode shuffleMode: Int,
    minWidth: Int?,
    minHeight: Int?
): RemoteViews {

    val layoutId = R.layout.widget_player
    val remoteViews = RemoteViews(context.packageName, layoutId)

    val hasRoomForRepeatAndShuffleModes = hasRoomForRepeatAndShuffleModes(minWidth, minHeight)

    // the play button image
    remoteViews.setImageViewResource(R.id.btn_play, if (isPlaying) R.drawable.ic_pause_18dp else R.drawable.ic_play_18dp)

    // the current item title
    remoteViews.setTextViewText(R.id.tv_song_name, currItemTitle)
    val horizontalCells = getCellsForSize(size = minWidth ?: 0)
    val songNameTextSizeInSp = when {
        horizontalCells >= 4 -> 13.5f
        horizontalCells >= 3 -> 12.8f
        horizontalCells >= 2 -> 12f
        else -> 12f
    }
    remoteViews.setTextViewTextSize(R.id.tv_song_name, TypedValue.COMPLEX_UNIT_SP, songNameTextSizeInSp)

    // the play button
    val toggleIntent = newIntentFromWidget(context, PlayerService.COMMAND_TOGGLE)
    val togglePi = PendingIntent(context, RC_COMMAND_TOGGLE, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.btn_play, togglePi)

    // the previous button
    val previousIntent = newIntentFromWidget(context, PlayerService.COMMAND_SKIP_TO_PREVIOUS)
    val previousPi = PendingIntent(context, RC_COMMAND_SKIP_TO_PREVIOUS, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.btn_skip_to_previous, previousPi)

    // the next button
    val nextIntent = newIntentFromWidget(context, PlayerService.COMMAND_SKIP_TO_NEXT)
    val nextPi = PendingIntent(context, RC_COMMAND_SKIP_TO_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.btn_skip_to_next, nextPi)

    if (hasRoomForRepeatAndShuffleModes) {
        // the repeat mode
        val repeatModeIntent = newIntentFromWidget(context, PlayerService.COMMAND_SWITCH_TO_NEXT_REPEAT_MODE)
        val repeatModePi = PendingIntent(context, RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE, repeatModeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_repeat_mode, repeatModePi)
        val repeatModeIconRes = when (repeatMode) {
            Player.REPEAT_OFF -> R.drawable.ic_repeat_disabled
            Player.REPEAT_PLAYLIST -> R.drawable.ic_repeat_all_enabled
            Player.REPEAT_ONE -> R.drawable.ic_repeat_one_enabled
            else -> R.drawable.ic_repeat_disabled
        }
        remoteViews.setImageViewResource(R.id.btn_repeat_mode, repeatModeIconRes)
        remoteViews.setViewVisibility(R.id.btn_repeat_mode, View.VISIBLE)

        // the shuffle mode
        val shuffleModeIntent = newIntentFromWidget(context, PlayerService.COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE)
        val shuffleModePi = PendingIntent(context, RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE, shuffleModeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_shuffle_mode, shuffleModePi)
        val shuffleModeIconRes = when (shuffleMode) {
            Player.SHUFFLE_OFF -> R.drawable.ic_shuffle_disabled
            Player.SHUFFLE_ON -> R.drawable.ic_shuffle_enabled
            else -> R.drawable.ic_shuffle_disabled
        }
        remoteViews.setImageViewResource(R.id.btn_shuffle_mode, shuffleModeIconRes)
        remoteViews.setViewVisibility(R.id.btn_shuffle_mode, View.VISIBLE)
    } else {
        remoteViews.setViewVisibility(R.id.btn_repeat_mode, View.GONE)
        remoteViews.setViewVisibility(R.id.btn_shuffle_mode, View.GONE)
    }

    // the root layout
    val appIntent = newIntent(context, true)
    val appPi = PendingIntent.getActivity(context, RC_OPEN_PLAYER, appIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.root_view, appPi)

    // the album art
    val albumArtVisibility = if (hasRoomForAlbumArt(minWidth, minHeight)) View.VISIBLE else View.GONE
    remoteViews.setViewVisibility(R.id.imv_album_art, albumArtVisibility)

    return remoteViews
}

private fun loadArt(context: Context, albumArtId: Long?, remoteViews: RemoteViews, vararg widgetIds: Int) {
    val target = AppWidgetTarget(context, R.id.imv_album_art, remoteViews, *widgetIds)
    val requestOptions = RequestOptions.circleCropTransform().override(180, 180)
    if (albumArtId != null) {
        val err = Glide.with(context)
            .asBitmap()
            .load(R.drawable.widget_album_art_placeholder)
            .apply(requestOptions)

        // overriding default request options
        val defaultRequestOptions = GlideAlbumArtHelper.get()
            .makeRequestOptions(albumArtId)
            .placeholder(R.drawable.widget_album_art_placeholder)
            .error(R.drawable.widget_album_art_placeholder)
        val uri = GlideAlbumArtHelper.getUri(albumArtId)
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .apply(requestOptions.apply(defaultRequestOptions))
            .error(err)
            .into(target)
    } else {
        Glide.with(context)
            .asBitmap()
            .load(R.drawable.widget_album_art_placeholder)
            .apply(requestOptions)
            .into(target)
    }
}

/**
 * Calculates number of cells needed for the given size of the widget.
 */
private fun getCellsForSize(size: Int): Int {
    var n = 2
    while (70 * n - 30 < size) {
        ++n
    }
    return n - 1
}