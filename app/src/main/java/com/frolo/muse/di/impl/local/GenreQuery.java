package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.muse.model.media.Genre;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;


final class GenreQuery {
    private static final Uri URI =
            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

    private static final Query.Builder<Genre> BUILDER =
            new Query.Builder<Genre>() {
                @Override
                public Genre build(Cursor cursor, String[] projection) {
                    return new Genre(
                            cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                            cursor.getString(cursor.getColumnIndex(PROJECTION[1]))
                    );
                }
            };

    static final class Sort {
        // Sort orders are case-insensitive

        static final String BY_NAME = MediaStore.Audio.Genres.NAME + " COLLATE NOCASE ASC";

        private Sort() {
        }
    }

    private static final String[] PROJECTION = {
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME
    };

    /*package*/ static Flowable<List<Genre>> queryAll(
            ContentResolver resolver,
            String sortOrder
    ) {
        final String selection = null;
        final String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER);
    }

    /*package*/ static Flowable<List<Genre>> queryAll(
            ContentResolver resolver,
            String sortOrder,
            int minSongDuration
    ) {
        final String selection = null;
        final String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER
        ).map(new Function<List<Genre>, List<Genre>>() {
            @Override
            public List<Genre> apply(List<Genre> genres) {
                if (minSongDuration <= 0)
                    return genres;

                try {
                    final List<Genre> filtered = new ArrayList<>(genres.size());
                    for (Genre genre : genres) {
                        int maxSongDuration = SongQuery.getMaxSongDurationInGenre(resolver, genre);
                        if (maxSongDuration / 1000 >= minSongDuration) {
                            filtered.add(genre);
                        }
                    }
                    return filtered;
                } catch (Throwable ignored) {
                    return genres;
                }
            }
        });
    }

    /*package*/ static Flowable<List<Genre>> queryAll(
            ContentResolver resolver
    ) {
        return queryAll(resolver, Sort.BY_NAME);
    }

    /*package*/ static Flowable<List<Genre>> queryAllFiltered(
            ContentResolver resolver,
            String filter) {
        final String selection = MediaStore.Audio.Genres.NAME + " LIKE ?";
        final String[] selectionArgs = new String[]{ "%" + filter + "%" };
        final String sortOrder = Sort.BY_NAME;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER);
    }

    /*package*/ static Flowable<Genre> querySingle(
            final ContentResolver resolver,
            final long itemId) {
        return Query.querySingle(
                resolver,
                URI,
                PROJECTION,
                itemId,
                BUILDER
        );
    }

    /*package*/ static Flowable<Genre> querySingleByName(
            final ContentResolver resolver,
            final String name) {
        String selection = MediaStore.Audio.Genres.NAME + " = ?";
        String[] selectionArgs = new String[] { name };
        final String sortOrder = Sort.BY_NAME;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER)
                .map(new Function<List<Genre>, Genre>() {
                    @Override
                    public Genre apply(List<Genre> genres) throws Exception {
                        if (genres != null && genres.size() > 0) {
                            return genres.get(0);
                        } else {
                            return null;
                        }
                    }
                });
    }

    private GenreQuery() {
    }
}
