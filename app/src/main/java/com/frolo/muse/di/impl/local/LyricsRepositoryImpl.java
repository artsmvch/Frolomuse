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
import com.frolo.muse.repository.LyricsRepository;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;

public class LyricsRepositoryImpl implements LyricsRepository {

    private static final String[] PROJECTION = { AppMediaStore.Lyrics.TEXT };
    private final Context context;

    public LyricsRepositoryImpl(Context context) {
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
                    if (query.moveToFirst()) {
                        String text = query.getString(query.getColumnIndex(PROJECTION[0]));
                        return new Lyrics(text);
                    }
                    query.close();
                }
                throw new NullPointerException("Np lyrics found for the song: " + song);
            }
        });
    }

    @Override
    public Completable setLyrics(final Song song, final Lyrics lyrics) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                Uri uri = AppMediaStore.Lyrics.getContentUri();
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(AppMediaStore.Lyrics._ID, song.getId());
                values.put(AppMediaStore.Lyrics.TEXT, lyrics.getText());
                values.put(AppMediaStore.Lyrics.TIME_ADDED, System.currentTimeMillis());
                resolver.insert(uri, values);
            }
        });
    }
}
