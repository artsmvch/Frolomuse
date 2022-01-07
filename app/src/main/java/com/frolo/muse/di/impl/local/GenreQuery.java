package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.music.model.Genre;
import com.frolo.music.model.SongFilter;
import com.frolo.rxcontent.CursorMapper;
import com.frolo.rxcontent.RxContent;

import java.util.List;

import io.reactivex.Flowable;


/* package-private */ final class GenreQuery {

    static final class Sort {
        // Sort orders are case-insensitive

        static final String BY_NAME = MediaStore.Audio.Genres.NAME + " COLLATE NOCASE ASC";

        private Sort() {
        }
    }

    private static final Uri URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

    private static final CursorMapper<Genre> CURSOR_MAPPER = new CursorMapper<Genre>() {
        @Override
        public Genre map(Cursor cursor) {
            return new Genre(
                cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                cursor.getString(cursor.getColumnIndex(PROJECTION[1]))
            );
        }
    };

    private static final String[] PROJECTION = {
        MediaStore.Audio.Genres._ID,
        MediaStore.Audio.Genres.NAME
    };

    static Flowable<List<Genre>> queryAll(ContentResolver resolver, SongFilter songFilter, String sortOrder) {
        final String selection = null;
        final String[] selectionArgs = null;
        Flowable<List<Genre>> source = RxContent.query(resolver, URI, PROJECTION,
                selection, selectionArgs, sortOrder, ContentExecutors.workerExecutor(), CURSOR_MAPPER);
        return SongQueryHelper.filterGenres(resolver, source, songFilter);
    }

    static Flowable<List<Genre>> queryAll(ContentResolver resolver, SongFilter songFilter) {
        return queryAll(resolver, songFilter, Sort.BY_NAME);
    }

    static Flowable<List<Genre>> queryAllFiltered(ContentResolver resolver, SongFilter songFilter, String namePiece) {
        final String selection = MediaStore.Audio.Genres.NAME + " LIKE ?";
        final String[] selectionArgs = new String[] { "%" + namePiece + "%" };
        final String sortOrder = Sort.BY_NAME;
        Flowable<List<Genre>> source = RxContent.query(resolver, URI, PROJECTION,
                selection, selectionArgs, sortOrder, ContentExecutors.workerExecutor(), CURSOR_MAPPER);
        return SongQueryHelper.filterGenres(resolver, source, songFilter);
    }

    static Flowable<Genre> queryItem(ContentResolver resolver, long itemId) {
        return RxContent.queryItem(resolver, URI, PROJECTION, itemId, ContentExecutors.workerExecutor(), CURSOR_MAPPER);
    }

    static Flowable<Genre> queryItemByName(ContentResolver resolver, String name) {
        String selection = MediaStore.Audio.Genres.NAME + " = ?";
        String[] selectionArgs = new String[] { name };
        final String sortOrder = Sort.BY_NAME;
        return RxContent.query(resolver, URI, PROJECTION,
                selection, selectionArgs, sortOrder, ContentExecutors.workerExecutor(), CURSOR_MAPPER)
                .map(genres -> genres.get(0));
    }

    private GenreQuery() {
    }
}
