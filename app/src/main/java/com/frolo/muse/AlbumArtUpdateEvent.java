package com.frolo.muse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public final class AlbumArtUpdateEvent extends BroadcastReceiver {

    private static final String ACTION = "com.frolo.muse.ALBUM_ART_UPDATE";

    private static final String EXTRA_ALBUM_ID = "album_id";
    private static final String EXTRA_ART_DATA = "art_data";

    private static final IntentFilter INTENT_FILTER =
            new IntentFilter(ACTION);

    public interface Listener {
        void onUpdate(long albumId, @Nullable String artData);
    }

    public static void dispatch(
            @NonNull Context context,
            long albumId,
            String artData) {
        Intent intent = new Intent(ACTION)
                .putExtra(EXTRA_ALBUM_ID, albumId)
                .putExtra(EXTRA_ART_DATA, artData);

        LocalBroadcastManager
                .getInstance(context)
                .sendBroadcast(intent);
    }

    public static AlbumArtUpdateEvent register(
            @NonNull Context context,
            @NonNull Listener listener) {
        AlbumArtUpdateEvent event = new AlbumArtUpdateEvent(listener);
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(event, new IntentFilter(ACTION));
        return event;
    }

    public static AlbumArtUpdateEvent create(@NonNull Listener listener) {
        return new AlbumArtUpdateEvent(listener);
    }

    private Listener mListener;

    private AlbumArtUpdateEvent(Listener l) {
        this.mListener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.equals(ACTION)) {
            long albumId = intent.getLongExtra(EXTRA_ALBUM_ID, -1);
            String artData = intent.getStringExtra(EXTRA_ART_DATA);
            mListener.onUpdate(albumId, artData);
        }
    }

    public void register(@NonNull Context context) {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, INTENT_FILTER);
    }

    public void unregister(@NonNull Context context) {
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(this);
    }
}
