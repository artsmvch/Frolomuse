package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.frolo.muse.content.AppMediaStore;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.MediaFile;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.SongFilter;
import com.frolo.muse.model.media.SongWithPlayCount;
import com.frolo.rxcontent.CursorMapper;
import com.frolo.rxcontent.RxContent;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;


/* package-private */ final class SongQuery {

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

        static final String BY_DATE_ADDED =
                MediaStore.Audio.Media.DATE_ADDED + " ASC";

        // For playlist only
        static final String BY_PLAY_ORDER =
                MediaStore.Audio.Playlists.Members.PLAY_ORDER + " ASC";

        static final String BY_DURATION =
                MediaStore.Audio.Media.DURATION + " ASC";

        static final String BY_TRACK_NUMBER =
                MediaStore.Audio.Media.TRACK + " ASC";

        private Sort() {
        }
    }

    private static class SongPlayCount {
        final String absolutePath;
        final int playCount;
        final long lastPlayTime;

        SongPlayCount(String absolutePath, int playCount, long lastPlayTime) {
            this.absolutePath = absolutePath;
            this.playCount = playCount;
            this.lastPlayTime = lastPlayTime;
        }
    }

    private static final String[] PROJECTION_SONG_PLAY_COUNT = new String[] {
        AppMediaStore.SongPlayCount.ABSOLUTE_PATH,
        AppMediaStore.SongPlayCount.PLAY_COUNT,
        AppMediaStore.SongPlayCount.LAST_PLAY_TIME
    };

    private static final CursorMapper<SongPlayCount> CURSOR_MAPPER_SONG_PLAY_COUNT = new CursorMapper<SongPlayCount>() {
        @Override
        public SongPlayCount map(Cursor cursor) {
            String absolutePath = cursor.getString(
                    cursor.getColumnIndex(PROJECTION_SONG_PLAY_COUNT[0]));
            int playCount = cursor.getInt(
                    cursor.getColumnIndex(PROJECTION_SONG_PLAY_COUNT[1]));
            long lastPlayCount = cursor.getLong(
                    cursor.getColumnIndex(PROJECTION_SONG_PLAY_COUNT[2]));
            return new SongPlayCount(absolutePath, playCount, lastPlayCount);
        }
    };

    private static final Function<Object[], List<Song>> COMBINER = objects -> {
        List<Song> result = new ArrayList<>();
        for (Object obj : objects) {
            @SuppressWarnings("unchecked")
            List<Song> chunk = (List<Song>) obj;
            result.addAll(chunk);
        }
        return result;
    };

    static Flowable<Song> queryItem(ContentResolver resolver, long itemId) {
        return RxContent.queryItem(resolver, URI, SongQueryHelper.getSongProjection(), itemId,
                ContentExecutors.workerExecutor(), SongQueryHelper.getSongCursorMapper());
    }

    static Flowable<Song> queryItemByPath(ContentResolver resolver, String path) {
        String selection = MediaStore.Audio.Media.DATA + "=?";
        String[] selectionArgs = new String[] { path };
        String sortOrder = null;

        return RxContent.query(resolver, URI, SongQueryHelper.getSongProjection(), selection, selectionArgs,
                sortOrder, ContentExecutors.workerExecutor(), SongQueryHelper.getSongCursorMapper())
                .map(songs -> songs.get(0));
    }

    static Flowable<List<Song>> queryAllFavourites(ContentResolver resolver) {
        final Uri favUri = AppMediaStore.Favourites.getContentUri();
        final Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final List<Uri> uris = Arrays.asList(favUri, songsUri);
        return RxContent.createFlowable(resolver, uris, ContentExecutors.workerExecutor(),
            () -> {
                String[] projection = { AppMediaStore.Favourites.PATH };
                String selectionNull = null;
                String[] selectionArgsNull = null;
                String sortOrderNull = null;
                Cursor cursor = resolver.query(favUri, projection, selectionNull, selectionArgsNull, sortOrderNull);

                if (cursor == null) {
                    throw Query.genNullCursorErr(favUri);
                }

                List<Song> items = new ArrayList<>(cursor.getCount());
                try {
                    if (cursor.moveToFirst()) {
                        do {
                            String path = cursor.getString(cursor.getColumnIndex(projection[0]));

                            try {
                                Song item = queryItemByPath(resolver, path)
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
            });
    }

    @Deprecated
    static Flowable<List<Song>> queryForAlbums(ContentResolver resolver, Collection<Album> albums) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(albums.size());
        for (Album album : albums) {
            sources.add(query(resolver, SongFilter.allEnabled(), Sort.BY_TITLE, album));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    @Deprecated
    static Flowable<List<Song>> queryForArtists(ContentResolver resolver, Collection<Artist> artists) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(artists.size());
        for (Artist artist : artists) {
            sources.add(query(resolver, SongFilter.allEnabled(), Sort.BY_TITLE, artist));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    @Deprecated
    static Flowable<List<Song>> queryForGenres(ContentResolver resolver, Collection<Genre> genres) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(genres.size());
        for (Genre genre : genres) {
            sources.add(query(resolver, SongFilter.allEnabled(), Sort.BY_TITLE, genre));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    static Flowable<List<Song>> queryForPlaylist(ContentResolver resolver, Playlist playlist, String sortOrder) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.getId());
        String selection = null;
        String[] selectionArgs = null;
        return RxContent.query(resolver, uri, SongQueryHelper.getPlaylistMemberProjection(), selection, selectionArgs,
                sortOrder, ContentExecutors.workerExecutor(), SongQueryHelper.getPlaylistMemberCursorMapper());
    }

    @Deprecated
    static Flowable<List<Song>> queryForPlaylists(ContentResolver resolver, Collection<Playlist> playlists) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(playlists.size());
        for (Playlist playlist : playlists) {
            sources.add(queryForPlaylist(resolver, playlist, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    @Deprecated
    static Flowable<List<Song>> queryForMyFile(ContentResolver resolver, MyFile myFile, String sortOrder) {
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
        return RxContent.query(resolver, URI, SongQueryHelper.getSongProjection(), selection, selectionArgs,
                sortOrder, ContentExecutors.workerExecutor(), SongQueryHelper.getSongCursorMapper());
    }

    @Deprecated
    static Flowable<List<Song>> queryForMyFiles(ContentResolver resolver, Collection<MyFile> myFiles) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(myFiles.size());
        for (MyFile myFile : myFiles) {
            sources.add(queryForMyFile(resolver, myFile, Sort.BY_TITLE));
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    @Deprecated
    static Flowable<List<Song>> queryForMediaFiles(ContentResolver resolver, Collection<MediaFile> mediaFiles) {
        List<Flowable<List<Song>>> sources = new ArrayList<>(mediaFiles.size());
        for (MediaFile mediaFile : mediaFiles) {
            Flowable<List<Song>> source = queryItem(resolver, mediaFile.getId())
                .map(song -> Collections.singletonList(song));
            sources.add(source);
        }
        return Flowable.combineLatest(sources, COMBINER);
    }

    static Flowable<Boolean> isFavourite(ContentResolver resolver, Song item) {
        final String[] emptyProjection = new String[0];
        return RxContent.createFlowable(resolver, AppMediaStore.Favourites.getContentUri(), ContentExecutors.workerExecutor(),
            () -> {
                Uri uri = ContentUris.withAppendedId(
                        AppMediaStore.Favourites.getContentUri(), item.getId());
                Cursor cursor = resolver.query(uri, emptyProjection, null, null, null);
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
        );
    }

    static Completable changeFavourite(ContentResolver resolver, Song item) {
        return Completable.fromCallable((Callable<Boolean>) () -> {
            Uri uri = ContentUris.withAppendedId(AppMediaStore.Favourites.getContentUri(), item.getId());
            Cursor cursor = resolver.query(uri, new String[0], null, null, null);

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
                    int deletedCount = resolver.delete(AppMediaStore.Favourites.getContentUri(), selection, selectionArgs);

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
        });
    }

    static Completable update(
            ContentResolver resolver, Song item, String title, String album, String artist, String genre) {
        return Completable.fromAction(() -> {
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
        });
    }

    //region SongWithPlayCount queries

    /**
     * Returns a flowable that emits {@link SongWithPlayCount} item for the given <code>song</code>.
     *
     * @param resolver content resolver.
     * @param song to query the play count.
     * @return a flowable that emits {@link SongWithPlayCount}.
     */
    static Flowable<SongWithPlayCount> getSongWithPlayCount(ContentResolver resolver, Song song) {
        final String targetPath = song.getSource();
        return Flowable.create((FlowableOnSubscribe<String>) emitter -> {
            if (!emitter.isCancelled()) {
                final SongPlayCounter.Watcher w = new SongPlayCounter.Watcher() {
                    @Override
                    public void onChanged(String absolutePath) {
                        if (targetPath.equals(absolutePath)) {
                            emitter.onNext(absolutePath);
                        }
                    }
                };

                SongPlayCounter.startWatching(w);

                emitter.setDisposable(Disposables.fromAction(() -> SongPlayCounter.stopWatching(w)));
            }

            if (!emitter.isCancelled()) {
                emitter.onNext(targetPath);
            }
        }, BackpressureStrategy.LATEST)
            .map(ignored -> {
                final Uri uri = AppMediaStore.SongPlayCount.getContentUri();

                final String[] projection = new String[] {
                    AppMediaStore.SongPlayCount.PLAY_COUNT,
                    AppMediaStore.SongPlayCount.LAST_PLAY_TIME
                };

                final String selection = AppMediaStore.SongPlayCount.ABSOLUTE_PATH + "=?";
                final String[] selectionArgs = new String[] { song.getSource() };

                Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);

                if (cursor == null) {
                    throw Query.genNullCursorErr(uri);
                }

                final int playCount;
                final Long lastPlayTime;
                try {
                    if (cursor.moveToFirst()) {
                        playCount = cursor.getInt(cursor.getColumnIndex(projection[0]));
                        lastPlayTime = cursor.getLong(cursor.getColumnIndex(projection[1]));
                    } else {
                        playCount = 0;
                        lastPlayTime = null;
                    }
                } finally {
                    cursor.close();
                }

                return new SongWithPlayCount(song, playCount, lastPlayTime);
            });
    }

    static Flowable<List<SongWithPlayCount>> querySongsWithPlayCount(
            ContentResolver resolver, SongFilter songFilter, int minPlayCount) {
        final Uri songPlayCountUri = AppMediaStore.SongPlayCount.getContentUri();
        if (minPlayCount > 0) {
            final String selection = AppMediaStore.SongPlayCount.PLAY_COUNT + ">= ?";
            final String[] selectionArgs = new String[] { String.valueOf(minPlayCount) };
            return RxContent.query(resolver, songPlayCountUri, PROJECTION_SONG_PLAY_COUNT,
                selection, selectionArgs, null, ContentExecutors.workerExecutor(), CURSOR_MAPPER_SONG_PLAY_COUNT)
                .switchMap((Function<List<SongPlayCount>, Publisher<List<SongWithPlayCount>>>) counts -> {
                    if (counts.isEmpty()) {
                        // Returning this flowable because Flowable.combineLatest for empty collection will not work for us
                        return Flowable.just(Collections.<SongWithPlayCount>emptyList());
                    }

                    final List<Flowable<SongWithPlayCount>> sources = new ArrayList<>(counts.size());
                    for (final SongPlayCount count : counts) {
                        Flowable<SongWithPlayCount> source = queryItemByPath(resolver, count.absolutePath)
                            .map(song -> new SongWithPlayCount(song, count.playCount, count.lastPlayTime));
                        sources.add(source);
                    }
                    return Flowable.combineLatest(sources, objects -> {
                        List<SongWithPlayCount> result = new ArrayList<>(objects.length);
                        for (Object obj : objects) {
                            SongWithPlayCount item = (SongWithPlayCount) obj;
                            result.add(item);
                        }
                        return result;
                    });
                });
        } else {
            return query(resolver, songFilter, Sort.BY_TITLE)
                .switchMap((Function<List<Song>, Publisher<List<SongWithPlayCount>>>) songs -> {
                    if (songs.isEmpty()) {
                        // return this flowable because Flowable.combineLatest for empty collection will not work for us
                        return Flowable.just(Collections.<SongWithPlayCount>emptyList());
                    }

                    List<Flowable<SongWithPlayCount>> sources = new ArrayList<>(songs.size());
                    for (final Song song : songs) {
                        Flowable<SongWithPlayCount> source =
                            getSongWithPlayCount(resolver, song);

                        sources.add(source);
                    }

                    return Flowable.combineLatest(sources, objects -> {
                        List<SongWithPlayCount> items = new ArrayList<>(objects.length);
                        for (Object obj : objects) {
                            items.add((SongWithPlayCount) obj);
                        }
                        return items;
                    });
                });
        }
    }

    static Completable addSongPlayCount(ContentResolver resolver, Song song, int delta) {
        return Completable.fromAction(() -> {
            final Uri uri = AppMediaStore.SongPlayCount.getContentUri();
            final String[] projection = new String[] { AppMediaStore.SongPlayCount.PLAY_COUNT };
            final String selection = AppMediaStore.SongPlayCount.ABSOLUTE_PATH + "=?";
            final String[] selectionArgs = new String[] { song.getSource() };
            Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);

            if (cursor == null) {
                throw Query.genNullCursorErr(uri);
            }

            final int currentPlayCount;
            final boolean entityExists;
            try {
                if (cursor.moveToFirst()) {
                    entityExists = true;
                    currentPlayCount = cursor.getInt(cursor.getColumnIndex(projection[0]));
                } else {
                    entityExists = false;
                    currentPlayCount = 0;
                }
            } finally {
                cursor.close();
            }

            final int updatedPlayCount = currentPlayCount + delta;
            final long lastPlayTime = System.currentTimeMillis();

            if (entityExists) {
                ContentValues values = new ContentValues(2);
                values.put(AppMediaStore.SongPlayCount.PLAY_COUNT, updatedPlayCount);
                values.put(AppMediaStore.SongPlayCount.LAST_PLAY_TIME, lastPlayTime);
                int updatedCount = resolver.update(uri, values, selection, selectionArgs);
                if (updatedCount == 0) {
                    // TODO: throw an exception
                }
            } else {
                ContentValues values = new ContentValues(3);
                values.put(AppMediaStore.SongPlayCount.ABSOLUTE_PATH, song.getSource());
                values.put(AppMediaStore.SongPlayCount.PLAY_COUNT, updatedPlayCount);
                values.put(AppMediaStore.SongPlayCount.LAST_PLAY_TIME, lastPlayTime);
                Uri resultUri = resolver.insert(uri, values);
                if (resultUri == null) {
                    // TODO: throw an exception
                }
            }

            SongPlayCounter.dispatchChanged(song.getSource());
        });
    }

    //endregion

    static Flowable<List<Song>> query(final ContentResolver resolver, final Uri uri,
                                      final SongFilter filter, final String sortOrder) {
        if (filter.isAllDisabled()) {
            // Everything is disabled => return empty list
            return Flowable.just(Collections.emptyList());
        }

        if (filter.isAllEnabled()) {
            // Everything is enabled => return as is
            return RxContent.query(resolver, uri, SongQueryHelper.getSongProjection(), null, null,
                    sortOrder, ContentExecutors.workerExecutor(), SongQueryHelper.getSongCursorMapper());
        }

        SongQueryHelper.SelectionWithArgs selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter);

        return RxContent.query(resolver, uri, SongQueryHelper.getSongProjection(), selectionWithArgs.selection, selectionWithArgs.args,
                sortOrder, ContentExecutors.workerExecutor(), SongQueryHelper.getSongCursorMapper());
    }

    static Flowable<List<Song>> query(final ContentResolver resolver, final SongFilter filter, final String sortOrder) {
        return query(resolver, URI, filter, sortOrder);
    }

    static Flowable<List<Song>> query(final ContentResolver resolver, SongFilter filter,
                                      final String sortOrder, final Album album) {
        filter = filter.newBuilder()
            .setAlbumId(album.getId())
            .build();
        return query(resolver, filter, sortOrder);
    }

    static Flowable<List<Song>> query(final ContentResolver resolver, SongFilter filter,
                                      final String sortOrder, final Artist artist) {
        filter = filter.newBuilder()
            .setArtistId(artist.getId())
            .build();
        return query(resolver, filter, sortOrder);
    }

    static Flowable<List<Song>> query(final ContentResolver resolver, SongFilter filter,
                                      final String sortOrder, final Genre genre) {
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre.getId());
//        filter = filter.newBuilder()
//            .setGenreId(genre.getId())
//            .build();
        return query(resolver, uri, filter, sortOrder);
    }

    static Flowable<List<Song>> query(final ContentResolver resolver, SongFilter filter,
                                      final String sortOrder, final MyFile myFile) {
        File javaFile = myFile.getJavaFile();
        String absolutePath = javaFile.getAbsolutePath();
        if (javaFile.isFile()) {
            filter = filter.newBuilder()
                .allTypes()
                .setFilepath(absolutePath)
                .build();
        } else {
            filter = filter.newBuilder()
                .allTypes()
                .setFolderPath(absolutePath)
                .build();
        }
        return query(resolver, filter, sortOrder);
    }

    private SongQuery() {
    }

}
