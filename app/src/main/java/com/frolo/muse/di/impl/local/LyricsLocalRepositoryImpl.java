package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.frolo.muse.db.AppMediaStore;
import com.frolo.muse.model.lyrics.Lyrics;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.repository.LyricsLocalRepository;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;


public final class LyricsLocalRepositoryImpl implements LyricsLocalRepository {

    private static final String[] EMPTY_PROJECTION = { };
    private static final String[] PROJECTION = { AppMediaStore.Lyrics.TEXT };

    private final Context context;

    public LyricsLocalRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public Single<Lyrics> getLyrics(final Song song) {
        return Single.fromCallable(new Callable<Lyrics>() {
            @Override
            public Lyrics call() throws Exception {
                Uri uri = ContentUris.withAppendedId(AppMediaStore.Lyrics.getContentUri(), song.getId());
                ContentResolver resolver = context.getContentResolver();
                Cursor query = resolver.query(uri, PROJECTION, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            String text = query.getString(query.getColumnIndex(PROJECTION[0]));
                            return new Lyrics(text);
                        }
                    } finally {
                        query.close();
                    }
                }
                throw new NullPointerException("Lyrics not found for song: " + song);
            }
        });
    }

    @Override
    public Completable setLyrics(final Song song, final Lyrics lyrics) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                ContentResolver resolver = context.getContentResolver();
                Uri uri = AppMediaStore.Lyrics.getContentUri();
                ContentValues values = new ContentValues();
                values.put(AppMediaStore.Lyrics.TEXT, lyrics.getText());
                values.put(AppMediaStore.Lyrics.TIME_ADDED, System.currentTimeMillis());
                final boolean entityExists;
                long itemId = song.getId();
                Uri itemUri = ContentUris.withAppendedId(uri, itemId);
                try (Cursor cursor = resolver.query(itemUri, EMPTY_PROJECTION, null, null, null)) {
                    entityExists = cursor != null && cursor.moveToFirst();
                }
                if (entityExists) {
                    String selection = AppMediaStore.Lyrics._ID + " = ?";
                    String[] selectionArgs = new String[] { String.valueOf(itemId) };
                    resolver.update(uri, values, selection, selectionArgs);
                } else {
                    values.put(AppMediaStore.Lyrics._ID, itemId);
                    resolver.insert(uri, values);
                }
            }
        });
    }
}
