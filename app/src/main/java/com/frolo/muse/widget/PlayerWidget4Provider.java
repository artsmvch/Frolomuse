package com.frolo.muse.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.frolo.muse.R;
import com.frolo.muse.Logger;
import com.frolo.muse.engine.AudioSource;
import com.frolo.muse.engine.Player;
import com.frolo.muse.engine.service.PlayerService;
import com.frolo.muse.glide.GlideAlbumArtHelper;
import com.frolo.muse.ui.main.MainActivity;

import static com.frolo.muse.widget.Helper.RC_COMMAND_SKIP_TO_NEXT;
import static com.frolo.muse.widget.Helper.RC_COMMAND_SKIP_TO_PREVIOUS;
import static com.frolo.muse.widget.Helper.RC_COMMAND_SWITCH_TO_NEXT_REPEAT_MODE;
import static com.frolo.muse.widget.Helper.RC_COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE;
import static com.frolo.muse.widget.Helper.RC_COMMAND_TOGGLE;
import static com.frolo.muse.widget.Helper.RC_OPEN_PLAYER;
import static com.frolo.muse.widget.Helper.getPendingIntent;


public class PlayerWidget4Provider extends AppWidgetProvider {
    private static final String TAG = PlayerWidget4Provider.class.getSimpleName();

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
        Logger.d(TAG, "Widget enabled");
    }

    @Override
    public void onDisabled(Context context) {
        Logger.d(TAG, "Widget enabled");
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

        loadArt(context, player, views, appWidgetId);
    }

    static RemoteViews createWidgetLayout(Context context, @Nullable Player player) {
        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.widget_4_player);

        boolean isPlaying = (player != null && player.isPlaying());
        views.setImageViewResource(R.id.btn_play, isPlaying ? R.drawable.ic_cpause : R.drawable.ic_play);
        AudioSource current;
        if (player != null && ((current = player.getCurrent()) != null)) {
            views.setTextViewText(R.id.tv_song_name, current.getMetadata().getTitle());
        } else {
            views.setTextViewText(R.id.tv_song_name, context.getString(R.string.placeholder_unknown));
        }

        // play button
        Intent toggleIntent = PlayerService.newIntent(context, PlayerService.COMMAND_TOGGLE);
        PendingIntent togglePi = getPendingIntent(context, RC_COMMAND_TOGGLE, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_play, togglePi);

        //previous button
        Intent previousIntent = PlayerService.newIntent(context, PlayerService.COMMAND_SKIP_TO_PREVIOUS);
        PendingIntent previousPi = getPendingIntent(context, RC_COMMAND_SKIP_TO_PREVIOUS, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_skip_to_previous, previousPi);

        //next button
        Intent nextIntent = PlayerService.newIntent(context, PlayerService.COMMAND_SKIP_TO_NEXT);
        PendingIntent nextPi = getPendingIntent(context, RC_COMMAND_SKIP_TO_NEXT, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_skip_to_next, nextPi);

        //repeat mode
        Intent repeatModeIntent = PlayerService.newIntent(context, PlayerService.COMMAND_SWITCH_TO_NEXT_REPEAT_MODE);
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
        Intent shuffleModeIntent = PlayerService.newIntent(context, PlayerService.COMMAND_SWITCH_TO_NEXT_SHUFFLE_MODE);
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

        Intent appIntent = MainActivity.newIntent(context, true);
        PendingIntent appPi = PendingIntent.getActivity(context, RC_OPEN_PLAYER, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.ll_root, appPi);

        return views;
    }

    public static void update(final Context context, final Player player) {
        Logger.d(TAG, "Checking if widget is enabled");
        ComponentName widget = new ComponentName(context, PlayerWidget4Provider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int ids[] = manager.getAppWidgetIds(widget);
        if (ids != null && ids.length > 0) {
            Logger.d(TAG, "Updating widget");
            RemoteViews remoteViews = PlayerWidget4Provider.createWidgetLayout(context, player);
            manager.updateAppWidget(widget, remoteViews);
            loadArt(context, player, remoteViews, ids);
        } else {
            Logger.d(TAG, "No need to update. Widget is disabled");
        }
    }

    private static void loadArt(final Context context, Player player, RemoteViews views, int... ids) {
        final AppWidgetTarget widgetTarget = new AppWidgetTarget(
                context,
                R.id.imv_album_art,
                views,
                ids);
        final RequestOptions commonRequestOptions = RequestOptions.circleCropTransform().override(180, 180);
        AudioSource current = null;
        if (player != null && ((current = player.getCurrent()) != null)) {
            RequestBuilder<Bitmap> err = Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.widget_album_art_placeholder)
                    .apply(commonRequestOptions);

            // overriding default request options
            final RequestOptions defaultRequestOptions = GlideAlbumArtHelper.get()
                    .makeRequestOptions(current.getMetadata().getAlbumId())
                    .placeholder(R.drawable.widget_album_art_placeholder)
                    .error(R.drawable.widget_album_art_placeholder);

            final Uri uri = GlideAlbumArtHelper.getUri(current.getMetadata().getAlbumId());

            Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(commonRequestOptions.apply(defaultRequestOptions))
                    .error(err)
                    .into(widgetTarget);
        } else {
            Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.widget_album_art_placeholder)
                    .apply(commonRequestOptions)
                    .into(widgetTarget);
        }
    }
}
