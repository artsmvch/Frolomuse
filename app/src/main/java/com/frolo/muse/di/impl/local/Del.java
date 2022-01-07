package com.frolo.muse.di.impl.local;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.FrolomuseApp;
import com.frolo.music.model.Album;
import com.frolo.music.model.Artist;
import com.frolo.music.model.Genre;
import com.frolo.music.model.Media;
import com.frolo.music.model.MediaFile;
import com.frolo.music.model.MyFile;
import com.frolo.music.model.Playlist;
import com.frolo.music.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;


final class Del {
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final Uri URI_SONG = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION_SONG = new String[] {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA
    };

    private static final int RC_DELETE_MEDIA = 5037;

    private static void deleteMediaItems_Internal(ContentResolver resolver, Uri uri, Collection<? extends Media> items) {

        // Step 1: delete media items from the Media Store DB
        {
            try {
                StringBuilder opDeleteSelectionBuilder = new StringBuilder(BaseColumns._ID + " IN (");

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

                int deletedCount = resolver.delete(uri, opDeleteSelection, null);
                resolver.notifyChange(uri, null);

            } catch (Throwable error) {
                for (Media item : items) {
                    String selection = BaseColumns._ID + " = ?";
                    String[] selectionArgs = new String[] { String.valueOf(item.getId()) };
                    int deletedCount = resolver.delete(uri, selection, selectionArgs);
                    resolver.notifyChange(uri, null);
                }
            }
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
                } else if (media.getKind() == Media.PLAYLIST) {
                    String filepath = ((Playlist) media).getSource();
                    file = new File(filepath);
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
                        throw new RuntimeException("Failed to delete file: " + f.getAbsolutePath());
                    }
                }
            }

            FileDeletion.dispatchDeleted(filesToDelete);
        }
    }

    private static void deleteSongFilesFromQuery_Internal(
            ContentResolver resolver, Uri uri, String selection, String[] selectionArgs) throws Exception {

        Cursor cursor = resolver.query(
                uri, PROJECTION_SONG, selection, selectionArgs, null);

        if (cursor == null) {
            throw Query.genNullCursorErr(URI_SONG);
        }

        List<String> filepathList = new ArrayList<>(cursor.getCount());

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

                    filepathList.add(filepath);

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

        for (String filepath : filepathList) {
            try {
                // Delete file from the disk
                File file = new File(filepath);
                boolean deletionResult = file.delete();
                if (deletionResult) {
                    //throw new IllegalStateException("Failed to delete song file: " + filepath);
                }
            } catch (Throwable ignored) {
            }
        }

        String opDeleteSelection = opDeleteSelectionBuilder.toString();

        int deletedCount = resolver
                .delete(URI_SONG, opDeleteSelection, null);
        resolver.notifyChange(uri, null);
    }

    private static List<Uri> filterUrisForDeletion(Context context, Collection<Uri> uris) {
        List<Uri> filteredUris = new ArrayList<>(uris.size());
        for (Uri uri : uris) {
            if (context.checkUriPermission(uri, Binder.getCallingPid(), Binder.getCallingUid(),
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                filteredUris.add(uri);
            }
        }
        return filteredUris;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static void deleteUris_API30(Context context, Collection<Uri> uris) throws Exception {
        PendingIntent pendingIntent =
                MediaStore.createDeleteRequest(context.getContentResolver(), filterUrisForDeletion(context, uris));
        FrolomuseApp frolomuseApp = (FrolomuseApp) context.getApplicationContext();
        Activity activity = frolomuseApp.getForegroundActivity();
        if (activity != null) {
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                    RC_DELETE_MEDIA, null, 0, 0, 0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static void deleteSongs_API30(Context context, Collection<? extends Song> songs) throws Exception {
        if (songs.isEmpty()) {
            return;
        }

        List<Uri> uris = new ArrayList<>(songs.size());
        for (Song song : songs) uris.add(ContentUris.withAppendedId(URI_SONG, song.getId()));
        deleteUris_API30(context, uris);
    }

    private static void deleteSongs_Internal(Context context, Collection<Song> songs) throws Exception {
        if (songs.isEmpty()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteSongs_API30(context, songs);
        } else {
            for (Song song : songs) {
                Uri uri = ContentUris.withAppendedId(URI_SONG, song.getId());
                deleteSongFilesFromQuery_Internal(context.getContentResolver(), uri, null, null);
            }
        }
    }

    private static void deleteGenres_Internal(Context context, Collection<Genre> genres) throws Exception {
        if (genres.isEmpty()) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        Collection<Song> songs = SongQuery.queryForGenres(resolver, genres)
                .firstOrError()
                .blockingGet();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteSongs_API30(context, songs);
        } else {
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs);
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, genres);
        }
    }

    private static void deleteArtists_Internal(Context context, Collection<Artist> artists) throws Exception {
        if (artists.isEmpty()) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        Collection<Song> songs = SongQuery.queryForArtists(resolver, artists)
                .firstOrError()
                .blockingGet();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteSongs_API30(context, songs);
        } else {
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs);
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, artists);
        }
    }

    private static void deleteAlbums_Internal(Context context, Collection<Album> albums) throws Exception {
        if (albums.isEmpty()) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        Collection<Song> songs = SongQuery.queryForAlbums(resolver, albums)
                .firstOrError()
                .blockingGet();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteSongs_API30(context, songs);
        } else {
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs);
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albums);
        }
    }

    private static void deletePlaylists_Internal(Context context, Collection<Playlist> playlists) throws Exception {
        if (playlists.isEmpty()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Uri playlistsUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            List<Uri> uris = new ArrayList<>(playlists.size());
            for (Playlist playlist : playlists) {
                Uri uri = ContentUris.withAppendedId(playlistsUri, playlist.getId());
                uris.add(uri);
            }
            deleteUris_API30(context, uris);
        } else {
            // We only delete playlist entities from the MediaStore, the songs in them remain intact
            deleteMediaItems_Internal(context.getContentResolver(), MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, playlists);
        }
    }

    private static void deleteMyFiles_Internal(Context context, Collection<MyFile> myFiles) throws Exception {
        if (myFiles.isEmpty()) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        Collection<Song> songs = SongQuery.queryForMyFiles(resolver, myFiles)
                .firstOrError()
                .blockingGet();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteSongs_API30(context, songs);
        } else {
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs);
        }
    }

    private static void deleteMediaFiles_Internal(Context context, Collection<MediaFile> mediaFiles) throws Exception {
        if (mediaFiles.isEmpty()) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        Collection<Song> songs = SongQuery.queryForMediaFiles(resolver, mediaFiles)
                .firstOrError()
                .blockingGet();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteSongs_API30(context, songs);
        } else {
            deleteMediaItems_Internal(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songs);
        }
    }

    /*package*/ static Completable deleteSongs(final Context context, final Collection<Song> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteSongs_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deleteAlbums(final Context context, final Collection<Album> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteAlbums_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deleteArtists(final Context context, final Collection<Artist> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteArtists_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deleteGenres(final Context context, final Collection<Genre> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteGenres_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deletePlaylists(final Context context, final Collection<Playlist> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deletePlaylists_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deleteMyFiles(final Context context, final Collection<MyFile> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteMyFiles_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deleteMediaFiles(final Context context, final Collection<MediaFile> items) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                deleteMediaFiles_Internal(context, items);
            }
        });
    }

    /*package*/ static Completable deleteSong(Context context, Song item) {
        return deleteSongs(context, Collections.singleton(item));
    }

    /*package*/ static Completable deleteAlbum(Context context, Album item) {
        return deleteAlbums(context, Collections.singleton(item));
    }

    /*package*/ static Completable deleteArtist(Context context, Artist item) {
        return deleteArtists(context, Collections.singleton(item));
    }

    /*package*/ static Completable deleteGenre(Context context, Genre item) {
        return deleteGenres(context, Collections.singleton(item));
    }

    /*package*/ static Completable deletePlaylist(Context context, Playlist item) {
        return deletePlaylists(context, Collections.singleton(item));
    }

    /*package*/ static Completable deleteMyFile(Context context, MyFile item) {
        return deleteMyFiles(context, Collections.singleton(item));
    }

    /*package*/ static Completable deleteMediaFile(Context context, MediaFile item) {
        return deleteMediaFiles(context, Collections.singleton(item));
    }

    private Del() {
    }
}
