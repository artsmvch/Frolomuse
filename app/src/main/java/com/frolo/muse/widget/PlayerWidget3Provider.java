package com.frolo.muse.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.frolo.muse.R;
import com.frolo.muse.Trace;
import com.frolo.muse.engine.Player;
import com.frolo.muse.engine.service.PlayerService;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.ui.main.MainActivity;

import static com.frolo.muse.widget.Helper.RC_COMMAND_SKIP_TO_NEXT;
import static com.frolo.muse.widget.Helper.RC_COMMAND_SKIP_TO_PREVIOUS;
import static com.frolo.muse.widget.Helper.RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE;
import static com.frolo.muse.widget.Helper.RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE;
import static com.frolo.muse.widget.Helper.RC_COMMAND_TOGGLE;
import static com.frolo.muse.widget.Helper.RC_OPEN_PLAYER;
import static com.frolo.muse.widget.Helper.getPendingIntent;

public class PlayerWidget3Provider extends AppWidgetProvider {
    private static final String TAG = PlayerWidget3Provider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {

        }
    }

    @Override
    public void onEnabled(Context context) {
        Trace.d(TAG, "Widget enabled");
    }

    @Override
    public void onDisabled(Context context) {
        Trace.d(TAG, "Widget enabled");
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        PlayerService.PlayerBinder binder =
                (PlayerService.PlayerBinder) peekService(context, new Intent(context, PlayerService.class));
        Player player = null;
        if (binder != null) {
            player = binder.getService();
        } else {
            // Service may not be created yet
        }
        updatePlayerWidget(context, appWidgetManager, appWidgetId, player);
    }

    private static void updatePlayerWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Player player) {
        RemoteViews views = createWidgetLayout(context, player);

        //updating widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static RemoteViews createWidgetLayout(Context context, @Nullable Player player) {
        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.widget_3_player);

        boolean isPlaying = (player != null && player.isPlaying());
        views.setImageViewResource(R.id.btn_play, isPlaying ? R.drawable.ic_cpause : R.drawable.ic_play);
        Song current;
        if (player != null && ((current = player.getCurrent()) != null)) {
            views.setTextViewText(R.id.tsw_song_name, current.getTitle());
        } else {
            views.setTextViewText(R.id.tsw_song_name, context.getString(R.string.placeholder_unknown));
        }

        // play button
        Intent toggleIntent = PlayerService.Companion.newIntent(context, PlayerService.COMMAND_TOGGLE);
        PendingIntent togglePi = getPendingIntent(context, RC_COMMAND_TOGGLE, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_play, togglePi);

        //previous button
        Intent previousIntent = PlayerService.Companion.newIntent(context, PlayerService.COMMAND_SKIP_TO_PREVIOUS);
        PendingIntent previousPi = getPendingIntent(context, RC_COMMAND_SKIP_TO_PREVIOUS, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_skip_to_previous, previousPi);

        //next button
        Intent nextIntent = PlayerService.Companion.newIntent(context, PlayerService.COMMAND_SKIP_TO_NEXT);
        PendingIntent nextPi = getPendingIntent(context, RC_COMMAND_SKIP_TO_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_skip_to_next, nextPi);

        //repeat mode
        Intent repeatModeIntent = PlayerService.Companion.newIntent(context, PlayerService.COMMAND_SWITCH_TO_NEXT_REPEAT_MODE);
        PendingIntent repeatModePi = getPendingIntent(context, RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE, repeatModeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_repeat_mode, repeatModePi);
        @Player.RepeatMode int repeatMode = player != null ? player.getRepeatMode() : Player.REPEAT_OFF;
        switch (repeatMode) {
            case Player.REPEAT_OFF: {
                views.setImageViewResource(R.id.btn_repeat_mode, R.drawable.ic_repeat_disabled);
                break;
            }
            case Player.REPEAT_PLAYLIST: {
                views.setImageViewResource(R.id.btn_repeat_mode, R.drawable.ic_repeat_all_enabled);
                break;
            }
            case Player.REPEAT_ONE: {
                views.setImageViewResource(R.id.btn_repeat_mode, R.drawable.ic_repeat_one_enabled);
                break;
            }
        }

        //shuffle mode
        Intent shuffleModeIntent = PlayerService.Companion.newIntent(context, PlayerService.COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE);
        PendingIntent shuffleModePi = getPendingIntent(context, RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE, shuffleModeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_shuffle_mode, shuffleModePi);
        @Player.ShuffleMode int shuffleMode = player != null ? player.getShuffleMode() : Player.SHUFFLE_OFF;
        switch (shuffleMode) {
            case Player.SHUFFLE_OFF: {
                views.setImageViewResource(R.id.btn_shuffle_mode, R.drawable.ic_shuffle_disabled);
                break;
            }
            case Player.SHUFFLE_ON: {
                views.setImageViewResource(R.id.btn_shuffle_mode, R.drawable.ic_shuffle_enabled);
                break;
            }
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent appPi = PendingIntent.getActivity(context, RC_OPEN_PLAYER, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.ll_root, appPi);

        return views;
    }


    public static void update(final Context context, final Player player) {
        Trace.d(TAG, "Checking if widget is enabled");
        ComponentName widget = new ComponentName(context, PlayerWidget3Provider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int ids[] = manager.getAppWidgetIds(widget);
        if (ids != null && ids.length > 0) {
            Trace.d(TAG, "Updating widget");
            RemoteViews remoteViews = PlayerWidget3Provider.createWidgetLayout(context, player);
            manager.updateAppWidget(widget, remoteViews);
        } else {
            Trace.d(TAG, "No need to update: widget is disabled");
        }
    }
}
