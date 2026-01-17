package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.muse.R;
import com.frolo.music.model.Playlist;
import com.frolo.rxcontent.CursorMapper;
import com.frolo.rxcontent.RxContent;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;


/* package-private */ final class PlaylistQuery {

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

    private static final Uri URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

    private static final boolean IS_FROM_SHARED_STORAGE = true;

    private static final String[] PROJECTION = {
        MediaStore.Audio.Playlists._ID,
        MediaStore.Audio.Playlists.DATA,
        MediaStore.Audio.Playlists.NAME,
        MediaStore.Audio.Playlists.DATE_ADDED,
        MediaStore.Audio.Playlists.DATE_MODIFIED
    };

    private static final CursorMapper<Playlist> CURSOR_MAPPER = new CursorMapper<Playlist>() {
        @Override
        public Playlist map(Cursor cursor) {
            return new Playlist(
                cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                IS_FROM_SHARED_STORAGE,
                cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                cursor.getString(cursor.getColumnIndex(PROJECTION[2])),
                cursor.getLong(cursor.getColumnIndex(PROJECTION[3])),
                cursor.getLong(cursor.getColumnIndex(PROJECTION[4]))
            );
        }
    };

    private static boolean checkPlaylistNameExistsInternal(ContentResolver resolver, String name) {
        boolean exists = false;

        String[] projection = new String[] { MediaStore.Audio.Playlists.NAME };
        String selection = MediaStore.Audio.Playlists.NAME + "=?";
        String[] selectionArgs = new String[] { name };
        String sortOrder = null;
        Cursor cursor = resolver.query(URI, projection, selection, selectionArgs, sortOrder);

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

    private static long getPlaylistIdByNameInternal(ContentResolver resolver, String playlistName) {
        long id = -1;

        String[] projection = new String[] { MediaStore.Audio.Playlists._ID };
        String selection = MediaStore.Audio.Playlists.NAME + "=?";
        String[] selectionArgs = new String[] { playlistName };
        String sortOrder = null;
        Cursor cursor = resolver.query(URI, projection, selection, selectionArgs, sortOrder);

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

    static Flowable<List<Playlist>> queryAll(ContentResolver resolver, String sortOrder) {
        String selection = null;
        String[] selectionArgs = null;
        return RxContent.query(resolver, URI, PROJECTION, selection, selectionArgs,
                sortOrder, ContentExecutors.workerExecutor(), CURSOR_MAPPER);
    }

    static Flowable<List<Playlist>> queryAllFiltered(ContentResolver resolver, String filter) {
        String selection = MediaStore.Audio.Playlists.NAME + " LIKE ?";
        String[] selectionArgs = new String[] { "%" + filter + "%" };
        return RxContent.query(resolver, URI, PROJECTION, selection, selectionArgs,
                Sort.BY_NAME, ContentExecutors.workerExecutor(), CURSOR_MAPPER);
    }

    static Flowable<Playlist> queryItem(ContentResolver resolver, long itemId) {
        return RxContent.queryItem(resolver, URI, PROJECTION, itemId,
                ContentExecutors.workerExecutor(), CURSOR_MAPPER);
    }

    static Single<Playlist> create(Context context, ContentResolver resolver, String name) {
        return Single.fromCallable(() -> {
            if (name.trim().isEmpty()) {
                String msg = context.getString(R.string.name_is_empty);
                throw new IllegalArgumentException(msg);
            }

            String[] emptyProjection = new String[0];
            String selection = MediaStore.Audio.Playlists.NAME + " = ?";
            String[] selectionArgs = new String[] { name };
            String sortOrder = null;
            Cursor cursor = resolver.query(URI, emptyProjection, selection, selectionArgs, sortOrder);

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

            long id = getPlaylistIdByNameInternal(resolver, name);

            if (id == -1) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, name);

                Uri uri = resolver.insert(URI, values);

                // Need to notify the content resolver about the insertion,
                // because it does not automatically receive notifications in some Android APIs (i.e. 30)
                resolver.notifyChange(uri != null ? uri : URI, null);

                if (uri == null) {
                    // The docs say it may be null
                    throw new Exception("Failed to insert item: " + values);
                }

                String idString = uri.getLastPathSegment();
                if (idString == null) {
                    throw new Exception("Failed to parse uri last segment: " + uri);
                }

                id = Long.parseLong(idString);
            }

            //long now = System.currentTimeMillis() / 1000;

            // Simply querying the newly created playlist by its ID
            return queryItem(resolver, id).blockingFirst();
        });
    }

    static Single<Playlist> update(Context context, ContentResolver resolver, Playlist item, String newName) {
        return Single.fromCallable(() -> {
            if (item.getName().equals(newName)) {
                // just return same item
                return new Playlist(item);
            }

            if (newName.trim().isEmpty()) {
                String msg = context.getString(
                        R.string.name_is_empty);
                throw new IllegalArgumentException(msg);
            }

            if (checkPlaylistNameExistsInternal(resolver, newName)) {
                String msg = context.getString(
                        R.string.such_name_already_exists);
                throw new IllegalArgumentException(msg);
            }

            long existingId = getPlaylistIdByNameInternal(resolver, newName);

            if (existingId == item.getMediaId().getSourceId())
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
                    values, MediaStore.Audio.Playlists._ID + " = " + item.getMediaId().getSourceId(), null);
            if (updatedCount == 0) {
                throw new Exception("Failed to update item: " + item);
            }

            long now = System.currentTimeMillis() / 1000;
            return new Playlist(item.getMediaId().getSourceId(), IS_FROM_SHARED_STORAGE, newName, item.getSource(), item.getDateAdded(), now);
        });
    }

    private PlaylistQuery() {
    }
}
