package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;


final class Del {
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final Uri URI_SONG =
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION_SONG = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA
    };

    private static void deleteMediaItems_Internal(
            final ContentResolver resolver,
            final Uri uri,
            final Collection<?extends Media> items) throws Exception {

        // Step 1: delete media items from the Media Store DB
        {
            StringBuilder opDeleteSelectionBuilder =
                    new StringBuilder(BaseColumns._ID + " IN (");

            boolean firstLooped = false;
            for (Media media : items) {
                if (firstLooped) {
                    opDeleteSelectionBuilder.append(',');
                }
                opDeleteSelectionBuilder.append(media.getId());

                firstLooped = true;
            }
            opDeleteSelectionBuilder.append(')');

            String opDeleteSelection = opDeleteSelectionBuilder.toString();

            int deletedCount = resolver
                    .delete(uri, opDeleteSelection, null);

            // TODO: check deletedCount
        }

        // Step 2: delete files that belongs to the media items
        {
            List<File> filesToDelete = new ArrayList<>();
            for (Media media : items) {
                final File file;

                if (media.getKind() == Media.SONG) {
                    String src = ((Song) media).getSource();
                    file = new File(src);
                } else if (media.getKind() == Media.MY_FILE) {
                    file = ((MyFile) media).getJavaFile();
                } else {
                    file = null;
                }

                if (file != null && file.exists()) {
                    filesToDelete.add(file);
                }
            }

            for (File f : filesToDelete) {
                boolean result = f.delete();
                if (!result) {
                    if (DEBUG) {
                        throw new RuntimeException(
                                "Failed to delete file " + f.getAbsolutePath()
                        );
                    }
                }
            }

            FileDeletion.dispatchDeleted(filesToDelete);
        }
    }

    private static void deleteSongFilesFromQuery_Internal(
            final ContentResolver resolver,
            final Uri uri,
            final String selection,
            final String[] selectionArgs) throws Exception {

        Cursor cursor = resolver.query(
                uri, PROJECTION_SONG, selection, selectionArgs, null);

        if (cursor == null) {
            throw Query.genNullCursorErr(URI_SONG);
        }

        StringBuilder opDeleteSelectionBuilder =
                new StringBuilder(MediaStore.Audio.Media._ID + " IN (");

        try {
            if (cursor.moveToFirst()) {
                boolean firstLooped = false;
                do {
                    long songId = cursor.getLong(
                            cursor.getColumnIndex(PROJECTION_SONG[0]));
                    String filepath = cursor.getString(
                            cursor.getColumnIndex(PROJECTION_SONG[1]));

                    try {
                        // Delete file from the disk
                        File file = new File(filepath);
                        boolean deletedResult = file.delete();
                        // TODO: check deletedResult
                    } catch (Throwable ignored) {
                    }

                    if (firstLooped) {
                        opDeleteSelectionBuilder.append(',');
                    }
                    opDeleteSelectionBuilder.append(songId);

                    firstLooped = true;
                } while (cursor.moveToNext());
            }

            opDeleteSelectionBuilder.append(')');
        } finally {
            cursor.close();
        }

        String opDeleteSelection = opDeleteSelectionBuilder.toString();

        int deletedCount = resolver
                .delete(URI_SONG, opDeleteSelection, null);

        // TODO: check deletedCount
    }

    private static void deleteSongs_Internal(
            ContentResolver resolver,
            Collection<Song> songs) throws Exception {

        for (Song song : songs) {
            Uri uri = ContentUris.withAppendedId(
                    URI_SONG, song.getId());

            deleteSongFilesFromQuery_Internal(
                    resolver, uri, null, null);
        }
    }

    private static void deleteGenres_Internal(
            ContentResolver resolver,
            Collection<Genre> genres) throws Exception {

        Collection<Song> songs = SongQuery.queryForGenres(resolver, genres)
                .firstOrError()
                .blockingGet();

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songs);

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                genres);
    }

    private static void deleteArtists_Internal(
            ContentResolver resolver,
            Collection<Artist> artists) throws Exception {

        Collection<Song> songs = SongQuery.queryForArtists(resolver, artists)
                .firstOrError()
                .blockingGet();

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songs);

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                artists);
    }

    private static void deleteAlbums_Internal(
            ContentResolver resolver,
            Collection<Album> albums) throws Exception {

        Collection<Song> songs = SongQuery.queryForAlbums(resolver, albums)
                .firstOrError()
                .blockingGet();

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songs);

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                albums);
    }

    private static void deletePlaylists_Internal(
            ContentResolver resolver,
            Collection<Playlist> playlists) throws Exception {

        // Delete only playlist entities from the MediaStore

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                playlists);
    }

    private static void deleteMyFiles_Internal(
            ContentResolver resolver,
            Collection<MyFile> myFiles) throws Exception {

        Collection<Song> songs = SongQuery.queryForMyFiles(resolver, myFiles)
                .firstOrError()
                .blockingGet();

        deleteMediaItems_Internal(
                resolver,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songs);

        // TODO: Do we need to delete the file/folder as well?
    }

    /*package*/ static Completable deleteSongs(
            final ContentResolver resolver,
            final Collection<Song> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteSongs_Internal(resolver, items);
            }
        });
    }

    /*package*/ static Completable deleteAlbums(
            final ContentResolver resolver,
            final Collection<Album> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteAlbums_Internal(resolver, items);
            }
        });
    }

    /*package*/ static Completable deleteArtists(
            final ContentResolver resolver,
            final Collection<Artist> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteArtists_Internal(resolver, items);
            }
        });
    }

    /*package*/ static Completable deleteGenres(
            final ContentResolver resolver,
            final Collection<Genre> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteGenres_Internal(resolver, items);
            }
        });
    }

    /*package*/ static Completable deletePlaylists(
            final ContentResolver resolver,
            final Collection<Playlist> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deletePlaylists_Internal(resolver, items);
            }
        });
    }

    /*package*/ static Completable deleteMyFiles(
            final ContentResolver resolver,
            final Collection<MyFile> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteMyFiles_Internal(resolver, items);
            }
        });
    }

    /*package*/ static Completable deleteSong(
            final ContentResolver resolver,
            final Song item) {
        return deleteSongs(resolver, Collections.singleton(item));
    }

    /*package*/ static Completable deleteAlbum(
            final ContentResolver resolver,
            final Album item) {
        return deleteAlbums(resolver, Collections.singleton(item));
    }

    /*package*/ static Completable deleteArtist(
            final ContentResolver resolver,
            final Artist item) {
        return deleteArtists(resolver, Collections.singleton(item));
    }

    /*package*/ static Completable deleteGenre(
            final ContentResolver resolver,
            final Genre item) {
        return deleteGenres(resolver, Collections.singleton(item));
    }

    /*package*/ static Completable deletePlaylist(
            final ContentResolver resolver,
            final Playlist item) {
        return deletePlaylists(resolver, Collections.singleton(item));
    }

    /*package*/ static Completable deleteMyFile(
            final ContentResolver resolver,
            final MyFile item) {
        return deleteMyFiles(resolver, Collections.singleton(item));
    }

    private Del() {
    }
}
