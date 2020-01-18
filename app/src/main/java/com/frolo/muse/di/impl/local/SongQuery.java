package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;

import com.frolo.muse.db.AppMediaStore;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.SongWithPlayCount;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;


final class SongQuery {

    private static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    static final class Sort {
        // Sort orders are case-insensitive

        // For albums, artists and genres only
        static final String BY_DEFAULT = "";

        static final String BY_TITLE =
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC";

        static final String BY_ALBUM =
                MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";

        static final String BY_ARTIST =
                MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC";

        // For playlist only
        static final String BY_PLAY_ORDER =
                MediaStore.Audio.Playlists.Members.PLAY_ORDER + " ASC";

        static final String BY_DURATION =
                MediaStore.Audio.Media.DURATION + " ASC";

        private Sort() {
        }
    }

    private static final String[] PROJECTION_SONG = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR
    };

    private static final String[] PROJECTION_PLAYLIST_MEMBER = new String[] {
            MediaStore.Audio.Playlists.Members.AUDIO_ID,
            MediaStore.Audio.Playlists.Members.DATA,
            MediaStore.Audio.Playlists.Members.TITLE,
            MediaStore.Audio.Playlists.Members.ALBUM_ID,
            MediaStore.Audio.Playlists.Members.ALBUM,
            MediaStore.Audio.Playlists.Members.ARTIST_ID,
            MediaStore.Audio.Playlists.Members.ARTIST,
            MediaStore.Audio.Playlists.Members.DURATION,
            MediaStore.Audio.Playlists.Members.YEAR,
    };

    private static final String[] PROJECTION_SONG_PLAY_COUNT = new String[] {
            AppMediaStore.SongPlayCount.ABSOLUTE_PATH,
            AppMediaStore.SongPlayCount.ABSOLUTE_PATH
    };

    private static final Query.Builder<Song> BUILDER_SONG =
            new Query.Builder<Song>() {
        @Override
        public Song build(Cursor cursor, String[] projection) {
            return new Song(
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_SONG[0])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_SONG[1])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_SONG[2])),
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_SONG[3])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_SONG[4])),
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_SONG[5])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_SONG[6])),
                    "",
                    cursor.getInt(cursor.getColumnIndex(PROJECTION_SONG[7])),
                    cursor.getInt(cursor.getColumnIndex(PROJECTION_SONG[8]))
            );
        }
    };

    private static final Query.Builder<Song> BUILDER_PLAYLIST_MEMBER =
            new Query.Builder<Song>() {
        @Override
        public Song build(Cursor cursor, String[] projection) {
            return new Song(
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[0])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[1])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[2])),
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[3])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[4])),
                    cursor.getLong(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[5])),
                    cursor.getString(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[6])),
                    "",
                    cursor.getInt(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[7])),
                    cursor.getInt(cursor.getColumnIndex(PROJECTION_PLAYLIST_MEMBER[8]))
            );
        }
    };

    private static final Query.Builder<Pair<String, Integer>> BUILDER_SONG_PLAY_COUNT =
            new Query.Builder<Pair<String, Integer>>() {
                @Override
                public Pair<String, Integer> build(Cursor cursor, String[] projection) {
                    String absolutePath = cursor.getString(
                            cursor.getColumnIndex(PROJECTION_SONG_PLAY_COUNT[0]));
                    int playCount = cursor.getInt(
                            cursor.getColumnIndex(PROJECTION_SONG_PLAY_COUNT[1]));
                    return new Pair<>(absolutePath, playCount);
                }
            };

    private static final Function<Object[], List<Song>> COMBINER =
            new Function<Object[], List<Song>>() {
        @Override
        public List<Song> apply(Object[] objects) throws Exception {
            List<Song> result = new ArrayList<>();
            for (Object obj : objects) {
                @SuppressWarnings("unchecked")
                List<Song> items = (List<Song>) obj;
                result.addAll(items);
            }
            return result;
        }
    };

    /*package*/ static Flowable<List<Song>> queryAll(
            final ContentResolver resolver,
            final String sortOrder) {
        String selection = null;
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<List<Song>> queryAll(
            final ContentResolver resolver) {
        return queryAll(resolver, Sort.BY_TITLE);
    }

    /*package*/ static Flowable<List<Song>> queryAllFiltered(
            final ContentResolver resolver,
            final String filter) {
        final String selection = MediaStore.Audio.Media.TITLE + " LIKE ?";
        final String[] selectionArgs = new String[]{ "%" + filter + "%" };
        final String sortOrder = Sort.BY_TITLE;
        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<Song> querySingle(
            final ContentResolver resolver,
            final long itemId) {
        return Query.querySingle(
                resolver,
                URI,
                PROJECTION_SONG,
                itemId,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<Song> querySingleByPath(
            final ContentResolver resolver,
            final String path) {
        String selection = MediaStore.Audio.Media.DATA + "=?";
        String[] selectionArgs = new String[] { path };
        String sortOrder = null;

        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG)
                .map(new Function<List<Song>, Song>() {
                    @Override
                    public Song apply(List<Song> songs) throws Exception {
                        if (songs != null && songs.size() > 0) {
                            return songs.get(0);
                        } else {
                            return null;
                        }
                    }
                });
    }

    /*package*/ static Flowable<List<Song>> queryAllFavourites(
            final ContentResolver resolver
    ) {
        final Uri favUri = AppMediaStore.Favourites.getContentUri();
        final Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final List<Uri> uris = Arrays.asList(favUri, songsUri);
        return Query.createFlowable(
                resolver,
                uris,
                new Callable<List<Song>>() {
                    @Override
                    public List<Song> call() throws Exception {
                        String[] projection = { AppMediaStore.Favourites.PATH };
                        String selectionNull = null;
                        String[] selectionArgsNull = null;
                        String sortOrderNull = null;
                        Cursor cursor = resolver.query(
                                favUri,
                                projection,
                                selectionNull,
                                selectionArgsNull,
                                sortOrderNull);

                        if (cursor == null) {
                            throw Query.genNullCursorErr(favUri);
                        }

                        List<Song> items = new ArrayList<>(cursor.getCount());
                        try {
                            if (cursor.moveToFirst()) {
                                do {
                                    String path = cursor.getString(
                                            cursor.getColumnIndex(projection[0]));

                                    try {
                                        Song item = querySingleByPath(
                                                resolver,
                                                path)
                                                .firstOrError()
                                                .blockingGet();

                                        items.add(item);
                                    } catch (Throwable ignored) {
                                    }
                                } while (cursor.moveToNext());
                            }
                        } finally {
                            cursor.close();
                        }

                        return items;
                    }
                });
    }

    /*package*/ static Flowable<List<Song>> queryForAlbum(
            final ContentResolver resolver,
            final Album album,
            final String sortOrder) {
        String selection = "is_music != 0 and album_id = " + album.getId();
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<List<Song>> queryForAlbums(
            final ContentResolver resolver,
            final Collection<Album> albums) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(albums.size());
        for (Album album : albums) {
            sources.add(queryForAlbum(resolver, album, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    /*package*/ static Flowable<List<Song>> queryForArtist(
            final ContentResolver resolver,
            final Artist artist,
            final String sortOrder) {
        String selection = "is_music != 0  and artist_id = " + artist.getId();
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<List<Song>> queryForArtists(
            final ContentResolver resolver,
            final Collection<Artist> artists) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(artists.size());
        for (Artist artist : artists) {
            sources.add(queryForArtist(resolver, artist, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    /*package*/ static Flowable<List<Song>> queryForGenre(
            final ContentResolver resolver,
            final Genre genre,
            final String sortOrder) {
        Uri uri = MediaStore.Audio.Genres.Members
                .getContentUri("external", genre.getId());
        String selection = null;
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                uri,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<List<Song>> queryForGenres(
            final ContentResolver resolver,
            final Collection<Genre> genres) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(genres.size());
        for (Genre genre : genres) {
            sources.add(queryForGenre(resolver, genre, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    /*package*/ static Flowable<List<Song>> queryForPlaylist(
            final ContentResolver resolver,
            final Playlist playlist,
            final String sortOrder) {
        Uri uri = MediaStore.Audio.Playlists.Members
                .getContentUri("external", playlist.getId());
        String selection = null;
        String[] selectionArgs = null;
        return Query.query(
                resolver,
                uri,
                PROJECTION_PLAYLIST_MEMBER,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_PLAYLIST_MEMBER);
    }

    /*package*/ static Flowable<List<Song>> queryForPlaylists(
            final ContentResolver resolver,
            final Collection<Playlist> playlists) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(playlists.size());
        for (Playlist playlist : playlists) {
            sources.add(queryForPlaylist(resolver, playlist, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    /*package*/ static Flowable<List<Song>> queryForMyFile(
            final ContentResolver resolver,
            final MyFile myFile,
            final String sortOrder) {
        File javaFile = myFile.getJavaFile();
        String path = javaFile.getAbsolutePath();
        final String selection;
        final String[] selectionArgs;
        if (javaFile.isFile()) {
            // maybe it's an audio file
            selection = MediaStore.Audio.Media.DATA + " like ?";
            selectionArgs = new String[] {"%" + path + "%"};
        } else {
            // it's a folder, let's search it for audio files
            selection = MediaStore.Audio.Media.DATA + " like ?";
            selectionArgs = new String[] {"%" + path + "/%"};
        }
        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG
        );
    }

    /*package*/ static Flowable<List<Song>> queryForMyFiles(
            final ContentResolver resolver,
            final Collection<MyFile> myFiles) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(myFiles.size());
        for (MyFile myFile : myFiles) {
            sources.add(queryForMyFile(resolver, myFile, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    /*package*/ static Flowable<List<Song>> queryRecentlyAdded(
            final ContentResolver resolver,
            final long dateAdded
    ) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media.DATE_ADDED + ">" + dateAdded;
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        return Query.query(
                resolver,
                URI,
                PROJECTION_SONG,
                selection,
                selectionArgs,
                sortOrder,
                BUILDER_SONG);
    }

    /*package*/ static Flowable<Boolean> isFavourite(
            final ContentResolver resolver,
            final Song item
    ) {
        return Query.createFlowable(
                resolver,
                AppMediaStore.Favourites.getContentUri(),
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        Uri uri = ContentUris.withAppendedId(
                                AppMediaStore.Favourites.getContentUri(), item.getId());
                        Cursor cursor = resolver
                                .query(uri, null, null, null, null);
                        if (cursor != null) {
                            boolean isFavourite = false;
                            try {
                                isFavourite = cursor.moveToFirst();
                            } finally {
                                cursor.close();
                            }
                            return isFavourite;
                        } else {
                            return false;
                        }
                    }
                }
        );
    }

    /*package*/ static Completable changeFavourite(
            final ContentResolver resolver,
            final Song item
    ) {
        return Completable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Uri uri = ContentUris.withAppendedId(
                        AppMediaStore.Favourites.getContentUri(), item.getId());
                Cursor cursor = resolver.query(uri, null, null, null, null);

                if (cursor != null) {
                    boolean isFavourite = false;
                    try {
                        isFavourite = cursor.moveToFirst();
                    } finally {
                        cursor.close();
                    }

                    if (isFavourite) {
                        String selection = AppMediaStore.Favourites._ID + " = " + item.getId();
                        String[] selectionArgs = null;
                        int deletedCount = resolver.delete(
                                AppMediaStore.Favourites.getContentUri(),
                                selection,
                                selectionArgs);

                        if (deletedCount != 1) {
                            // TODO: throw an exception if it failed to delete
                        }
                        return false;
                    } else {
                        ContentValues values = new ContentValues();
                        values.put(AppMediaStore.Favourites._ID, item.getId());
                        values.put(AppMediaStore.Favourites.PATH, item.getSource());
                        values.put(AppMediaStore.Favourites.TIME_ADDED, System.currentTimeMillis());

                        Uri insertedCount = resolver.insert
                                (AppMediaStore.Favourites.getContentUri(),
                                        values);

                        if (insertedCount == null) {
                            // TODO: throw an exception if it failed to insert
                        }
                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
    }

    /*package*/ static Completable update(
            final ContentResolver resolver,
            final Song item,
            final String title,
            final String album,
            final String artist,
            final String genre
    ) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Audio.Media.TITLE, title);
                cv.put(MediaStore.Audio.Media.ALBUM, album);
                cv.put(MediaStore.Audio.Media.ARTIST, artist);

                final Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, item.getId());

                int updatedCount = resolver.update(uri, cv, null, null);

                resolver.notifyChange(uri, null);

                if (updatedCount == 0) {
                    // TODO: throw an exception if it failed to update
                }
            }
        });
    }

    /*package*/ static Flowable<List<SongWithPlayCount>> querySongsWithPlayCount(
            final ContentResolver resolver,
            final int minPlayCount
    ) {
        final Uri songPlayCountUri = AppMediaStore.SongPlayCount.getContentUri();
        if (minPlayCount > 0) {
            final String selection = AppMediaStore.SongPlayCount.PLAY_COUNT + ">= ?";
            final String[] selectionArgs = new String[] { String.valueOf(minPlayCount) };
            return Query.query(
                    resolver,
                    songPlayCountUri,
                    PROJECTION_SONG_PLAY_COUNT,
                    selection,
                    selectionArgs,
                    null,
                    BUILDER_SONG_PLAY_COUNT
            ).flatMap(new Function<List<Pair<String, Integer>>, Publisher<List<SongWithPlayCount>>>() {
                @Override
                public Publisher<List<SongWithPlayCount>> apply(List<Pair<String, Integer>> pairs) {
                    final List<Flowable<SongWithPlayCount>> sources = new ArrayList<>(pairs.size());
                    for (Pair<String, Integer> pair : pairs) {
                        final String absolutePath = pair.first;
                        final int playCount = pair.second;
                        Flowable<SongWithPlayCount> source =
                                querySingleByPath(resolver, absolutePath)
                                .map(new Function<Song, SongWithPlayCount>() {
                                    @Override
                                    public SongWithPlayCount apply(Song song) {
                                        return new SongWithPlayCount(song, playCount);
                                    }
                                });
                        sources.add(source);
                    }
                    return Flowable.combineLatest(
                            sources,
                            new Function<Object[], List<SongWithPlayCount>>() {
                                @Override
                                public List<SongWithPlayCount> apply(Object[] objects) {
                                    List<SongWithPlayCount> result = new ArrayList<>();
                                    for (Object obj : objects) {
                                        @SuppressWarnings("unchecked")
                                        List<SongWithPlayCount> items = (List<SongWithPlayCount>) obj;
                                        result.addAll(items);
                                    }
                                    return result;
                                }
                            }
                    );
                }
            });
        } else {
            return queryAll(resolver)
                    .map(new Function<List<Song>, List<SongWithPlayCount>>() {
                        @Override
                        public List<SongWithPlayCount> apply(List<Song> songs) {
                            final List<SongWithPlayCount> items = new ArrayList<>(songs.size());
                            final String[] playCountProjection = new String[] { AppMediaStore.SongPlayCount.PLAY_COUNT };
                            final String selection = AppMediaStore.SongPlayCount.ABSOLUTE_PATH + " =?";
                            for (Song song : songs) {
                                Cursor cursor = resolver.query(
                                        songPlayCountUri,
                                        playCountProjection,
                                        selection,
                                        new String[] { song.getSource() },
                                        null
                                );

                                final int playCount;
                                if (cursor != null) {
                                    try {
                                        playCount = cursor.getInt(
                                                cursor.getColumnIndex(playCountProjection[0]));
                                    } finally {
                                        cursor.close();
                                    }
                                } else {
                                    playCount = 0;
                                }

                                SongWithPlayCount item = new SongWithPlayCount(song, playCount);
                                items.add(item);
                            }
                            return items;
                        }
                    });
        }
    }

    private SongQuery() {
    }

}
