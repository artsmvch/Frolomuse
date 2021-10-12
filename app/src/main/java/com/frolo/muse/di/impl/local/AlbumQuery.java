package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.SongFilter;
import com.frolo.rxcontent.CursorMapper;
import com.frolo.rxcontent.RxContent;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;


/* package-private */ final class AlbumQuery {

    // Sort orders are case-insensitive
    static final class Sort {

        static final String BY_ALBUM = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
        static final String BY_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS + " ASC";

        private Sort() {
        }
    }

    private static final Uri URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

    private static final CursorMapper<Album> CURSOR_MAPPER =
        new CursorMapper<Album>() {
            @Override
            public Album map(Cursor cursor) {
                return new Album(
                    cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION[2])),
                    cursor.getInt(cursor.getColumnIndex(PROJECTION[3]))
                );
            }
        };

    private static final CursorMapper<Album> CURSOR_MAPPER_ARTIST_MEMBER =
        new CursorMapper<Album>() {
            @Override
            public Album map(Cursor cursor) {
                return new Album(
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[0])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[1])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[2])),
                    cursor.getInt(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[3]))
                );
            }
        };

    private static final String[] PROJECTION = {
        MediaStore.Audio.Albums._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Albums.ARTIST,
        MediaStore.Audio.Albums.NUMBER_OF_SONGS
    };

    private static final String[] PROJECTION_ARTIST_MEMBER = {
        MediaStore.Audio.Artists.Albums.ALBUM_ID,
        MediaStore.Audio.Artists.Albums.ALBUM,
        MediaStore.Audio.Artists.Albums.ARTIST,
        MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS
    };

    static Flowable<List<Album>> queryAll(
            final ContentResolver resolver, final SongFilter filter, final String sortOrder) {
        String selection = null;
        String[] selectionArgs = null;
        Flowable<List<Album>> source = RxContent.query(resolver, URI, PROJECTION, selection, selectionArgs,
                sortOrder, ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
        return SongQuery.filter(resolver, source, filter);
    }

    static Flowable<List<Album>> queryAllFiltered(
            ContentResolver resolver, SongFilter songFilter, String namePiece) {
        final String selection = MediaStore.Audio.Albums.ALBUM + " LIKE ?";
        final String[] selectionArgs = new String[]{ "%" + namePiece + "%" };
        Flowable<List<Album>> source =  RxContent.query(resolver, URI, PROJECTION, selection, selectionArgs,
                Sort.BY_ALBUM, ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
        return SongQuery.filter(resolver, source, songFilter);
    }

    static Flowable<Album> querySingle(ContentResolver resolver, long itemId) {
        return RxContent.queryItem(resolver, URI, PROJECTION, itemId,
                ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
    }

    static Flowable<Album> queryForPreview(ContentResolver resolver) {
        return queryAll(resolver, SongFilter.none(), Sort.BY_ALBUM).observeOn(Schedulers.computation()).map(albums -> {
            int minNumOfSongs = 5;
            int maxNumOfSongs = 10;
            Album candidate = null;
            int index = 0;
            do {
                Album album = albums.get(index);
                if (candidate != null) {
                    int currNumOfSongs = candidate.getNumberOfSongs();
                    int nextNumOfSongs = album.getNumberOfSongs();
                    if (currNumOfSongs < minNumOfSongs && nextNumOfSongs >= minNumOfSongs) {
                        candidate = album;
                    } else if (currNumOfSongs > maxNumOfSongs
                            && nextNumOfSongs >= minNumOfSongs && nextNumOfSongs <= maxNumOfSongs) {
                        candidate = album;
                    }
                } else {
                    candidate = album;
                }
                int numberOfSongs = candidate.getNumberOfSongs();
                if (numberOfSongs >= minNumOfSongs && numberOfSongs <= maxNumOfSongs) {
                    // We found it! The perfect album for the preview
                    return candidate;
                }
                index++;
            } while (index < albums.size());
            return candidate;
        });
    }

    static Flowable<List<Album>> queryForArtist(ContentResolver resolver, SongFilter songFilter, long artistId) {
        final Uri uri = MediaStore.Audio.Artists.Albums
                .getContentUri("external", artistId);
        final String selection = null;
        final String[] selectionArgs = null;

        final Flowable<List<Album>> source;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final String sortOrder = MediaStore.Audio.Artists.Albums.ALBUM + " COLLATE NOCASE ASC";
            source = RxContent.query(resolver, uri, PROJECTION_ARTIST_MEMBER, selection, selectionArgs,
                    sortOrder, ExecutorHolder.workerExecutor(), CURSOR_MAPPER_ARTIST_MEMBER);
        } else {
            final String sortOrder = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
            source = RxContent.query(resolver, uri, PROJECTION, selection, selectionArgs,
                    sortOrder, ExecutorHolder.workerExecutor(), CURSOR_MAPPER);
        }
        return SongQuery.filter(resolver, source, songFilter);
    }

    @Deprecated
    static Completable updateAlbumArtPath(
            final ContentResolver resolver, final long albumId, final String filepath) {
        return Completable.fromAction(
                () -> {
                    Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
                    Uri idAlbumArtUdi = ContentUris.withAppendedId(albumArtUri, albumId);

                    {
                        // First set _data to null
                        ContentValues values = new ContentValues();
                        values.putNull("_data");

                        final String selection = "album_id=?";
                        final String[] selectionArgs = new String[]{String.valueOf(albumId)};
                        final int updatedCount = resolver.update(idAlbumArtUdi, values, selection, selectionArgs);

                        if (updatedCount == 0) {
                            ContentValues newValues = new ContentValues(values);
                            newValues.put("album_id", albumId);
                            Uri uri = resolver.insert(albumArtUri, newValues);
                        }
                    }

                    if (filepath != null) {
                        // then insert filepath into _data if it's notl null
                        ContentValues insertionValues = new ContentValues();
                        insertionValues.put("_data", filepath);

                        final String selection = "album_id=?";
                        final String[] selectionArgs = new String[] {String.valueOf(albumId)};
                        resolver.update(idAlbumArtUdi, insertionValues, selection, selectionArgs);
                    } else {
                        // It was a deletion so we don't insert anything here
                    }

                    resolver.notifyChange(idAlbumArtUdi, null);
                }
        );
    }

    private AlbumQuery() {
    }
}
