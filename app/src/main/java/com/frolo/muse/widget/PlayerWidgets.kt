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
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.R
import com.frolo.muse.common.albumId
import com.frolo.muse.common.title
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.di.ApplicationComponent
import com.frolo.muse.di.applicationComponent
import com.frolo.player.AudioSource
import com.frolo.player.Player
import com.frolo.muse.engine.service.PlayerService.Companion.newIntentFromWidget
import com.frolo.muse.engine.service.PlayerServiceCmd
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

private data class PlayerWidgetParams(
    val currItem: AudioSource?,
    val isPlaying: Boolean,
    @Player.RepeatMode val repeatMode: Int,
    @Player.ShuffleMode val shuffleMode: Int
)

/**
 * Creates [PlayerWidgetParams] according to the current state of [player].
 * If the player is null or shutdown, the method retrieves the cached state from the repositories
 * and creates the params based on the cached data.
 */
private fun getPlayerWidgetParams(context: Context, player: Player?): PlayerWidgetParams {
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
        val frolomuseApp = context.applicationContext as? FrolomuseApp
        if (frolomuseApp != null) {
            val component: ApplicationComponent = context.applicationComponent
            val preferences = component.providePreferences()
            val songRepository = component.provideSongRepository()
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
    return PlayerWidgetParams(
        currItem = currItem,
        isPlaying = isPlaying,
        repeatMode = repeatMode,
        shuffleMode = shuffleMode
    )
}

/**
 * Updates [widgetId] player widget according to the given [params].
 * [newWidgetOptionsBundle] is used to get new widget options.
 */
private fun AppWidgetManager.updatePlayerWidget(
    context: Context,
    params: PlayerWidgetParams,
    widgetId: Int,
    newWidgetOptionsBundle: Bundle? = null
) {
    val widgetOptions = this.getWidgetOptions(widgetId, newWidgetOptionsBundle)

    val remoteViews = PlayerRemoteViews(
        context = context,
        params = params,
        widgetOptions = widgetOptions
    )

    this.updateAppWidget(widgetId, remoteViews)

    if (hasRoomForAlbumArt(widgetOptions)) {
        loadArt(
            context = context,
            albumArtId = params.currItem?.albumId,
            remoteViews = remoteViews,
            widgetIds = *intArrayOf(widgetId)
        )
    }
}

fun AppWidgetManager.updatePlayerWidget(
    context: Context,
    player: Player?,
    widgetId: Int,
    newWidgetOptionsBundle: Bundle? = null
) {
    val params = getPlayerWidgetParams(context, player)
    updatePlayerWidget(context, params, widgetId, newWidgetOptionsBundle)
}

fun AppWidgetManager.updatePlayerWidgets(
    context: Context,
    player: Player?,
    vararg widgetIds: Int
) {
    val params = getPlayerWidgetParams(context, player)
    widgetIds.forEach { widgetId ->
        updatePlayerWidget(context, params, widgetId)
    }
}

private fun hasRoomForAlbumArt(widgetOptions: WidgetOptions?): Boolean {
    if (widgetOptions == null) {
        return false
    }
    val horizontalCells = getCellsForSize(widgetOptions.minWidth)
    return horizontalCells >= 4
}

private fun hasRoomForRepeatAndShuffleModes(widgetOptions: WidgetOptions?): Boolean {
    if (widgetOptions == null) {
        return false
    }
    val horizontalCells = getCellsForSize(widgetOptions.minWidth)
    return horizontalCells >= 3
}

@Suppress("FunctionName")
private fun PlayerRemoteViews(
    context: Context,
    params: PlayerWidgetParams,
    widgetOptions: WidgetOptions?
): RemoteViews {

    val layoutId = R.layout.widget_player
    val remoteViews = RemoteViews(context.packageName, layoutId)

    val hasRoomForRepeatAndShuffleModes = hasRoomForRepeatAndShuffleModes(widgetOptions)

    // the play button image
    remoteViews.setImageViewResource(R.id.btn_play, if (params.isPlaying) R.drawable.wgt_ic_pause else R.drawable.wgt_ic_play)

    // the current item title
    remoteViews.setTextViewText(R.id.tv_song_name, params.currItem?.title.orEmpty())
    val horizontalCells = getCellsForSize(size = widgetOptions?.minWidth ?: 0)
    val songNameTextSizeInSp = when {
        horizontalCells >= 4 -> 13.5f
        horizontalCells >= 3 -> 12.8f
        horizontalCells >= 2 -> 12f
        else -> 12f
    }
    remoteViews.setTextViewTextSize(R.id.tv_song_name, TypedValue.COMPLEX_UNIT_SP, songNameTextSizeInSp)

    // the play button
    val toggleIntent = newIntentFromWidget(context, PlayerServiceCmd.CMD_TOGGLE)
    val togglePi = PendingIntent(context, RC_COMMAND_TOGGLE, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.btn_play, togglePi)

    // the previous button
    val previousIntent = newIntentFromWidget(context, PlayerServiceCmd.CMD_SKIP_TO_PREVIOUS)
    val previousPi = PendingIntent(context, RC_COMMAND_SKIP_TO_PREVIOUS, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.btn_skip_to_previous, previousPi)

    // the next button
    val nextIntent = newIntentFromWidget(context, PlayerServiceCmd.CMD_SKIP_TO_NEXT)
    val nextPi = PendingIntent(context, RC_COMMAND_SKIP_TO_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews.setOnClickPendingIntent(R.id.btn_skip_to_next, nextPi)

    if (hasRoomForRepeatAndShuffleModes) {
        // the repeat mode
        val repeatModeIntent = newIntentFromWidget(context, PlayerServiceCmd.CMD_CHANGE_REPEAT_MODE)
        val repeatModePi = PendingIntent(context, RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE, repeatModeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_repeat_mode, repeatModePi)
        val repeatModeIconRes = when (params.repeatMode) {
            Player.REPEAT_OFF -> R.drawable.wgt_ic_repeat_disabled
            Player.REPEAT_PLAYLIST -> R.drawable.wgt_ic_repeat_all_enabled
            Player.REPEAT_ONE -> R.drawable.wgt_ic_repeat_one_enabled
            else -> R.drawable.wgt_ic_repeat_disabled
        }
        remoteViews.setImageViewResource(R.id.btn_repeat_mode, repeatModeIconRes)
        remoteViews.setViewVisibility(R.id.btn_repeat_mode, View.VISIBLE)

        // the shuffle mode
        val shuffleModeIntent = newIntentFromWidget(context, PlayerServiceCmd.CMD_CHANGE_SHUFFLE_MODE)
        val shuffleModePi = PendingIntent(context, RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE, shuffleModeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.btn_shuffle_mode, shuffleModePi)
        val shuffleModeIconRes = when (params.shuffleMode) {
            Player.SHUFFLE_OFF -> R.drawable.wgt_ic_shuffle_disabled
            Player.SHUFFLE_ON -> R.drawable.wgt_ic_shuffle_enabled
            else -> R.drawable.wgt_ic_shuffle_disabled
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
    val albumArtVisibility = if (hasRoomForAlbumArt(widgetOptions)) View.VISIBLE else View.GONE
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