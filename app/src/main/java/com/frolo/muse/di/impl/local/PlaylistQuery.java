package com.frolo.muse.di.impl.local;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.muse.R;
import com.frolo.muse.model.media.Playlist;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;


final class PlaylistQuery {

    private static final Uri URI =
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION = {
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME,
            MediaStore.Audio.Playlists.DATE_ADDED,
            MediaStore.Audio.Playlists.DATE_MODIFIED };

    static final class Sort {
        // Sort orders are case-insensitive

        static final String BY_NAME =
                MediaStore.Audio.Playlists.NAME + " COLLATE NOCASE ASC";

        static final String BY_DATE_ADDED =
                MediaStore.Audio.Playlists.DATE_ADDED + " ASC";

        static final String BY_DATE_MODIFIED =
                MediaStore.Audio.Playlists.DATE_MODIFIED + " ASC";

        private Sort() {
        }
    }

    private static final Query.Builder<Playlist> BUILDER =
            new Query.Builder<Playlist>() {
                @Override
                public Playlist build(Cursor cursor, String[] projection) {
                    return new Playlist(
                            cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                            cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                            cursor.getLong(cursor.getColumnIndex(PROJECTION[2])),
                            cursor.getLong(cursor.getColumnIndex(PROJECTION[3]))
                    );
                }
            };

    private static boolean checkPlaylistNameExists_Internal(
            ContentResolver resolver,
            String name) {
        boolean exists = false;

        String[] projection = new String[] { MediaStore.Audio.Playlists.NAME };
        String selection = MediaStore.Audio.Playlists.NAME + "=?";
        String[] selectionArgs = new String[] { name };
        String sortOrder = null;
        Cursor cursor = resolver.query(
                URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);

        if (cursor != null) {
            try {
                if (cursor.getCount() >= 1) {
                    exists = true;
                }
            } finally {
                cursor.close();
            }
        }

        return exists;
    }

    private static long getPlaylistIdByName_Internal(
            ContentResolver resolver,
            String playlistName) {
        long id = -1;

        String[] projection = new String[] { MediaStore.Audio.Playlists._ID };
        String selection = MediaStore.Audio.Playlists.NAME + "=?";
        String[] selectionArgs = new String[] { playlistName };
        String sortOrder = null;
        Cursor cursor = resolver.query(
                URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);

        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    id = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }
        return id;
    }

    /*package*/ static Flowable<List<Playlist>> queryAll(
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

    /*package*/ static Flowable<List<Playlist>> queryAll(
            final ContentResolver resolver) {
        final String selection = null;
        final String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                Sort.BY_NAME,
                BUILDER);
    }

    /*package*/ static Flowable<List<Playlist>> queryAllFiltered(
            final ContentResolver resolver,
            final String filter) {
        final String selection = MediaStore.Audio.Playlists.NAME + " LIKE ?";
        final String[] selectionArgs = new String[]{ filter + "%" };
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                Sort.BY_NAME,
                BUILDER);
    }

    /*package*/ static Flowable<Playlist> querySingle(
            final ContentResolver resolver,
            final long itemId) {
        return Query.querySingle(
                resolver,
                URI,
                PROJECTION,
                itemId,
                BUILDER);
    }

    /*package*/ static Single<Playlist> create(
            final Context context,
            final ContentResolver resolver,
            final String name) {
        return Single.fromCallable(new Callable<Playlist>() {
            @Override
            public Playlist call() throws Exception {
                if (name.trim().isEmpty()) {
                    String msg = context.getString(
                            R.string.name_is_empty);
                    throw new IllegalArgumentException(msg);
                }

                String[] emptyProjection = new String[0];
                String selection = MediaStore.Audio.Playlists.NAME + " = ?";
                String[] selectionArgs = new String[] { name };
                String sortOrder = null;
                Cursor cursor = resolver.
                        query(URI, emptyProjection, selection, selectionArgs, sortOrder);

                boolean exists = false;

                if (cursor != null) {
                    try {
                        exists = cursor.getCount() != 0;
                    } finally {
                        cursor.close();
                    }
                }


                if (exists) {
                    String msg = context.getString(
                            R.string.such_name_already_exists);
                    throw new IllegalArgumentException(msg);
                }

                long id = getPlaylistIdByName_Internal(resolver, name);

                if (id == -1) {
                    ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Audio.Playlists.NAME, name);

                    Uri uri = resolver.insert(URI, values);

                    if (uri == null) {
                        // The docs say it may be null
                        throw new Exception(
                                "Failed to insert item: " + values);
                    }

                    String idString = uri.getLastPathSegment();
                    if (idString == null) {
                        throw new Exception(
                                "Failed to parse uri last segment: " + uri);
                    }

                    id = Long.parseLong(idString);
                }

                long now = System.currentTimeMillis() / 1000;
                return new Playlist(id, name, now, now);
            }
        });
    }

    /*package*/ static Single<Playlist> update(
            final Context context,
            final ContentResolver resolver,
            final Playlist item,
            final String newName) {
        return Single.fromCallable(new Callable<Playlist>() {
            @Override
            public Playlist call() throws Exception {
                if (item.getName().equals(newName)) {
                    // just return same item
                    return new Playlist(item);
                }

                if (newName.trim().isEmpty()) {
                    String msg = context.getString(
                            R.string.name_is_empty);
                    throw new IllegalArgumentException(msg);
                }

                if (checkPlaylistNameExists_Internal(resolver, newName)) {
                    String msg = context.getString(
                            R.string.such_name_already_exists);
                    throw new IllegalArgumentException(msg);
                }

                long existingId = getPlaylistIdByName_Internal(resolver, newName);

                if (existingId == item.getId())
                    // we're trying to change the name of the same item so return it
                    return item;
                if (existingId != -1) {
                    String msg = context.getString(
                            R.string.such_name_already_exists);
                    throw new IllegalArgumentException(msg);
                }

                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, newName);

                int updatedCount = resolver.update(URI,
                        values, MediaStore.Audio.Playlists._ID + " = " + item.getId(), null);
                if (updatedCount == 0) {
                    throw new Exception("Failed to updated item: " + item);
                }

                long now = System.currentTimeMillis() / 1000;
                return new Playlist(item.getId(), newName, item.getDateAdded(), now);
            }
        });
    }

    private PlaylistQuery() {
    }
}
