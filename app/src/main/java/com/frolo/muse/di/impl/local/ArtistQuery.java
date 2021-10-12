package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.SongFilter;
import com.frolo.rxcontent.CursorMapper;
import com.frolo.rxcontent.RxContent;

import java.util.List;

import io.reactivex.Flowable;


/* package-private */ final class ArtistQuery {

    static final class Sort {

        // Sort orders are case-insensitive
        static final String BY_ARTIST = MediaStore.Audio.Artists.ARTIST + " COLLATE NOCASE ASC";
        static final String BY_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS + " ASC";
        static final String BY_NUMBER_OF_TRACKS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS + " ASC";

        private Sort() {
        }
    }

    private static final Uri URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION = {
        MediaStore.Audio.Artists._ID,
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
    };

    private static final CursorMapper<Artist> CURSOR_MAPPER = new CursorMapper<Artist>() {
        @Override
        public Artist map(Cursor cursor) {
            return new Artist(
                cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                cursor.getInt(cursor.getColumnIndex(PROJECTION[2])),
                cursor.getInt(cursor.getColumnIndex(PROJECTION[3]))
            );
        }
    };

    static Flowable<List<Artist>> queryAll(ContentResolver resolver, SongFilter songFilter, String sortOrder) {
        final String selection = null;
        final String[] selectionArgs = null;
        Flowable<List<Artist>> source = RxContent.query(resolver, URI, PROJECTION,
                selection, selectionArgs, sortOrder, ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
        return SongQueryHelper.filterArtists(resolver, source, songFilter);
    }

    static Flowable<List<Artist>> queryAll(ContentResolver resolver, SongFilter songFilter) {
        return queryAll(resolver, songFilter, Sort.BY_ARTIST);
    }

    static Flowable<List<Artist>> queryAllFiltered(ContentResolver resolver, SongFilter songFilter, String namePiece) {
        final String selection = MediaStore.Audio.Artists.ARTIST + " LIKE ?";
        final String[] selectionArgs = new String[] { "%" + namePiece + "%" };
        final String sortOrder = Sort.BY_ARTIST;
        Flowable<List<Artist>> source = RxContent.query(resolver, URI, PROJECTION,
                selection, selectionArgs, sortOrder, ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
        return SongQueryHelper.filterArtists(resolver, source, songFilter);
    }

    static Flowable<Artist> queryItem(ContentResolver resolver, long itemId) {
        return RxContent.queryItem(resolver, URI, PROJECTION, itemId, ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
    }

    private ArtistQuery() {
    }
}
