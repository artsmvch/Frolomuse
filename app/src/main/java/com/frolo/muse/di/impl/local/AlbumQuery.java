package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.frolo.muse.model.media.Album;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


final class AlbumQuery {
    private static final Uri URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

    private static final Query.Builder<Album> BUILDER =
            new Query.Builder<Album>() {
                @Override
                public Album build(Cursor cursor, String[] projection) {
                    return new Album(
                            cursor.getLong(cursor.getColumnIndex(PROJECTION[0])),
                            cursor.getString(cursor.getColumnIndex(PROJECTION[1])),
                            cursor.getString(cursor.getColumnIndex(PROJECTION[2])),
                            cursor.getInt(cursor.getColumnIndex(PROJECTION[3]))
                    );
                }
            };

    private static final Query.Builder<Album> BUILDER_ARTIST_MEMBER =
            new Query.Builder<Album>() {
                @Override
                public Album build(Cursor cursor, String[] projection) {
                    return new Album(
                            cursor.getLong(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[0])),
                            cursor.getString(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[1])),
                            cursor.getString(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[2])),
                            cursor.getInt(cursor.getColumnIndex(PROJECTION_ARTIST_MEMBER[3]))
                    );
                }
            };

    // Sort orders are case-insensitive
    static final class Sort {

        static final String BY_ALBUM = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
        static final String BY_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS + " ASC";

        private Sort() {
        }
    }

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

    /*package*/ static Flowable<List<Album>> queryAll(
            ContentResolver resolver,
            String sortOrder
    ) {
        String selection = null;
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER
        );
    }

    /*package*/ static Flowable<List<Album>> queryAll(
            final ContentResolver resolver,
            final String sortOrder,
            final int minSongDuration
    ) {
        String selection = null;
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER
        ).map(new Function<List<Album>, List<Album>>() {
            @Override
            public List<Album> apply(List<Album> albums) {
                if (minSongDuration <= 0)
                    return albums;

                try {
                    final List<Album> filtered = new ArrayList<>(albums.size());
                    for (Album album : albums) {
                        int maxSongDuration = SongQuery.getMaxSongDurationInAlbum(resolver, album);
                        if (maxSongDuration / 1000 >= minSongDuration) {
                            filtered.add(album);
                        }
                    }
                    return filtered;
                } catch (Throwable ignored) {
                    return albums;
                }
            }
        });
    }

    /*package*/ static Flowable<List<Album>> queryAll(
            ContentResolver resolver
    ) {
        return queryAll(resolver, Sort.BY_ALBUM);
    }

    /*package*/ static Flowable<List<Album>> queryAllFiltered(
            ContentResolver resolver,
            String filter
    ) {
        final String selection = MediaStore.Audio.Albums.ALBUM + " LIKE ?";
        final String[] selectionArgs = new String[]{ "%" + filter + "%" };
        return Query.query(
                resolver,
                URI,
                PROJECTION,
                selection,
                selectionArgs,
                Sort.BY_ALBUM,
                BUILDER
        );
    }

    /*package*/ static Flowable<Album> querySingle(
            ContentResolver resolver,
            long itemId
    ) {
        return Query.querySingle(
                resolver,
                URI,
                PROJECTION,
                itemId,
                BUILDER
        );
    }

    /*package*/ static Flowable<Album> querySingleForPreview(ContentResolver resolver) {
        return queryAll(resolver).observeOn(Schedulers.computation()).map(new Function<List<Album>, Album>() {
            @Override
            public Album apply(@NonNull List<Album> albums) throws Exception {
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
                } while (index < albums.size());
                return candidate;
            }
        });
    }

    /*package*/ static Flowable<List<Album>> queryForArtist(
            ContentResolver resolver,
            long artistId
    ) {
        final Uri uri = MediaStore.Audio.Artists.Albums
                .getContentUri("external", artistId);
        final String selection = null;
        final String[] selectionArgs = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final String sortOrder = MediaStore.Audio.Artists.Albums.ALBUM + " COLLATE NOCASE ASC";
            return Query.query(
                    resolver,
                    uri,
                    PROJECTION_ARTIST_MEMBER,
                    selection,
                    selectionArgs,
                    sortOrder,
                    BUILDER_ARTIST_MEMBER
            );
        } else {
            final String sortOrder = MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
            return Query.query(
                    resolver,
                    uri,
                    PROJECTION,
                    selection,
                    selectionArgs,
                    sortOrder,
                    BUILDER
            );
        }
    }

    /*package*/ static Completable updateAlbumArtPath(
            final ContentResolver resolver,
            final long albumId,
            final String filepath) {

        return Completable.fromAction(
                new Action() {
                    @Override
                    public void run() throws Exception {
                        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
                        Uri idAlbumArtUdi = ContentUris.withAppendedId(albumArtUri, albumId);

                        {
                            // First set _data to null
                            ContentValues values = new ContentValues();
                            values.putNull("_data");

                            final String selection = "album_id=?";
                            final String[] selectionArgs = new String[]{String.valueOf(albumId)};
                            final int updatedCount = resolver.update(
                                    idAlbumArtUdi,
                                    values,
                                    selection,
                                    selectionArgs);

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
                }
        );
    }

    private AlbumQuery() {
    }
}
