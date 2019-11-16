package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.muse.model.media.Artist;

import java.util.List;

import io.reactivex.Flowable;


final class ArtistQuery {
    private static final Uri URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION = {
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
    };

    private static final Query.Builder<Artist> BUILDER =
            new Query.Builder<Artist>() {
        @Override
        public Artist build(Cursor cursor, String[] projection) {
            return new Artist(
                    cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                    cursor.getInt(cursor.getColumnIndex(PROJECTION[2])),
                    cursor.getInt(cursor.getColumnIndex(PROJECTION[3]))
            );
        }
    };

    static final class Sort {

        // Sort orders are case-insensitive
        static final String BY_ARTIST = MediaStore.Audio.Artists.ARTIST + " COLLATE NOCASE ASC";
        static final String BY_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS + " ASC";
        static final String BY_NUMBER_OF_TRACKS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " ASC";

        private Sort() {
        }
    }

    /*package*/ static Flowable<List<Artist>> queryAll(
            final ContentResolver resolver,
            final String sortOrder) {
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

    /*package*/ static Flowable<List<Artist>> queryAll(ContentResolver resolver) {
        return queryAll(resolver, Sort.BY_ARTIST);
    }

    /*package*/ static Flowable<List<Artist>> queryAllFiltered(
            ContentResolver resolver,
            String filter) {
        final String selection = MediaStore.Audio.Artists.ARTIST + " LIKE ?";
        final String[] selectionArgs = new String[]{ "%" + filter + "%" };
        final String sortOrder = Sort.BY_ARTIST;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER);
    }

    /*package*/ static Flowable<Artist> querySingle(
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

    private ArtistQuery() {
    }
}
