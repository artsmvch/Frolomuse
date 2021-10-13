package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.frolo.muse.DebugUtils;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.SongFilter;
import com.frolo.muse.model.media.SongType;
import com.frolo.muse.model.media.Songs;
import com.frolo.rxcontent.CursorMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;


final class SongQueryHelper {

    static final class SelectionWithArgs {
        final String selection;
        final String[] args;

        SelectionWithArgs(String selection, String[] args) {
            this.selection = selection;
            this.args = args;
        }
    }

    private static final String DEFAULT_GENRE_VALUE = "";

    private static final String[] OPT_EMPTY_PROJECTION = { BaseColumns._ID };

    private static final String[] PROJECTION_SONG = new String[] {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.IS_MUSIC,
        MediaStore.Audio.Media.IS_PODCAST,
        MediaStore.Audio.Media.IS_RINGTONE,
        MediaStore.Audio.Media.IS_ALARM,
        MediaStore.Audio.Media.IS_NOTIFICATION
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static final String[] PROJECTION_SONG_API_29 = new String[] {
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.TRACK,
        MediaStore.Audio.Media.IS_MUSIC,
        MediaStore.Audio.Media.IS_PODCAST,
        MediaStore.Audio.Media.IS_RINGTONE,
        MediaStore.Audio.Media.IS_ALARM,
        MediaStore.Audio.Media.IS_NOTIFICATION,
        MediaStore.Audio.Media.IS_AUDIOBOOK
    };

    static String[] getSongProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return PROJECTION_SONG_API_29;
        } else {
            return PROJECTION_SONG;
        }
    }

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
        MediaStore.Audio.Playlists.Members.TRACK,
        MediaStore.Audio.Playlists.Members.IS_MUSIC,
        MediaStore.Audio.Playlists.Members.IS_PODCAST,
        MediaStore.Audio.Playlists.Members.IS_RINGTONE,
        MediaStore.Audio.Playlists.Members.IS_ALARM,
        MediaStore.Audio.Playlists.Members.IS_NOTIFICATION
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static final String[] PROJECTION_PLAYLIST_MEMBER_API_29 = new String[] {
        MediaStore.Audio.Playlists.Members.AUDIO_ID,
        MediaStore.Audio.Playlists.Members.DATA,
        MediaStore.Audio.Playlists.Members.TITLE,
        MediaStore.Audio.Playlists.Members.ALBUM_ID,
        MediaStore.Audio.Playlists.Members.ALBUM,
        MediaStore.Audio.Playlists.Members.ARTIST_ID,
        MediaStore.Audio.Playlists.Members.ARTIST,
        MediaStore.Audio.Playlists.Members.DURATION,
        MediaStore.Audio.Playlists.Members.YEAR,
        MediaStore.Audio.Playlists.Members.TRACK,
        MediaStore.Audio.Playlists.Members.IS_MUSIC,
        MediaStore.Audio.Playlists.Members.IS_PODCAST,
        MediaStore.Audio.Playlists.Members.IS_RINGTONE,
        MediaStore.Audio.Playlists.Members.IS_ALARM,
        MediaStore.Audio.Playlists.Members.IS_NOTIFICATION,
        MediaStore.Audio.Playlists.Members.IS_AUDIOBOOK
    };

    static String[] getPlaylistMemberProjection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return PROJECTION_PLAYLIST_MEMBER_API_29;
        } else {
            return PROJECTION_PLAYLIST_MEMBER;
        }
    }

    private static final CursorMapper<Song> CURSOR_MAPPER_SONG = new CursorMapper<Song>() {
        @Override
        public Song map(Cursor cursor) {
            String[] projection = getSongProjection();
            return Songs.create(
                cursor.getLong(cursor.getColumnIndex(projection[0])),
                SongQueryHelper.getSongType(cursor),
                cursor.getString(cursor.getColumnIndex(projection[1])),
                cursor.getString(cursor.getColumnIndex(projection[2])),
                cursor.getLong(cursor.getColumnIndex(projection[3])),
                cursor.getString(cursor.getColumnIndex(projection[4])),
                cursor.getLong(cursor.getColumnIndex(projection[5])),
                cursor.getString(cursor.getColumnIndex(projection[6])),
                DEFAULT_GENRE_VALUE,
                cursor.getInt(cursor.getColumnIndex(projection[7])),
                cursor.getInt(cursor.getColumnIndex(projection[8])),
                cursor.getInt(cursor.getColumnIndex(projection[9]))
            );
        }
    };

    static CursorMapper<Song> getSongCursorMapper() {
        return CURSOR_MAPPER_SONG;
    }

    private static final CursorMapper<Song> CURSOR_MAPPER_PLAYLIST_MEMBER = new CursorMapper<Song>() {
        @Override
        public Song map(Cursor cursor) {
            String[] projection = getPlaylistMemberProjection();
            return Songs.create(
                cursor.getLong(cursor.getColumnIndex(projection[0])),
                SongQueryHelper.getSongType(cursor),
                cursor.getString(cursor.getColumnIndex(projection[1])),
                cursor.getString(cursor.getColumnIndex(projection[2])),
                cursor.getLong(cursor.getColumnIndex(projection[3])),
                cursor.getString(cursor.getColumnIndex(projection[4])),
                cursor.getLong(cursor.getColumnIndex(projection[5])),
                cursor.getString(cursor.getColumnIndex(projection[6])),
                DEFAULT_GENRE_VALUE,
                cursor.getInt(cursor.getColumnIndex(projection[7])),
                cursor.getInt(cursor.getColumnIndex(projection[8])),
                cursor.getInt(cursor.getColumnIndex(projection[9]))
            );
        }
    };

    static CursorMapper<Song> getPlaylistMemberCursorMapper() {
        return CURSOR_MAPPER_PLAYLIST_MEMBER;
    }

    static boolean getBool(@NonNull Cursor cursor, @NonNull String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex >= 0) {
            return getBool(cursor, cursor.getColumnIndex(columnName));
        } else {
            // fallback
            return false;
        }
    }

    static boolean getBool(@NonNull Cursor cursor, int columnIndex) {
        return cursor.getInt(columnIndex) != 0;
    }

    @NonNull
    static SongType getSongType(@NonNull Cursor cursor) {
        if (getBool(cursor, MediaStore.Audio.Media.IS_MUSIC)) {
            return SongType.MUSIC;
        } else if (getBool(cursor, MediaStore.Audio.Media.IS_PODCAST)) {
            return SongType.PODCAST;
        } else if (getBool(cursor, MediaStore.Audio.Media.IS_RINGTONE)) {
            return SongType.RINGTONE;
        } else if (getBool(cursor, MediaStore.Audio.Media.IS_ALARM)) {
            return SongType.ALARM;
        } else if (getBool(cursor, MediaStore.Audio.Media.IS_NOTIFICATION)) {
            return SongType.NOTIFICATION;
        } else if (getBool(cursor, MediaStore.Audio.Media.IS_AUDIOBOOK)) {
            return SongType.AUDIOBOOK;
        } else {
            // By default
            return SongType.MUSIC;
        }
    }

    @NonNull
    static SelectionWithArgs getSelectionWithArgs(@NonNull SongFilter filter) {
        StringBuilder selectionBuilder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        {
            // Song type
            Collection<SongType> includedTypes = filter.getTypes();
            boolean noTypesIncluded = includedTypes.isEmpty();
            boolean atLeastOneTypeDefined = false;
            loop : for (SongType type : SongType.values()) {
                boolean isTypeIncluded = includedTypes.contains(type);
                // If no types included at all, then each type must be specified as '== 0' in the selection
                if (isTypeIncluded || noTypesIncluded) {

                    if (selectionBuilder.length() > 0) {
                        if (atLeastOneTypeDefined) {
                            if (noTypesIncluded) {
                                selectionBuilder.append(" AND ");
                            } else {
                                selectionBuilder.append(" OR ");
                            }
                        } else {
                            selectionBuilder.append(" AND (");
                        }
                    } else {
                        selectionBuilder.append("(");
                    }

                    switch (type) {
                        case MUSIC:
                            selectionBuilder.append(MediaStore.Audio.Media.IS_MUSIC);
                            break;
                        case PODCAST:
                            selectionBuilder.append(MediaStore.Audio.Media.IS_PODCAST);
                            break;
                        case RINGTONE:
                            selectionBuilder.append(MediaStore.Audio.Media.IS_RINGTONE);
                            break;
                        case ALARM:
                            selectionBuilder.append(MediaStore.Audio.Media.IS_ALARM);
                            break;
                        case NOTIFICATION:
                            selectionBuilder.append(MediaStore.Audio.Media.IS_NOTIFICATION);
                            break;
                        case AUDIOBOOK: {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                selectionBuilder.append(MediaStore.Audio.Media.IS_AUDIOBOOK);
                            } else {
                                // Not supported
                                continue loop;
                            }
                            break;
                        }
                    }

                    if (noTypesIncluded) {
                        selectionBuilder.append(" = ?");
                        selectionArgsList.add("0");
                    } else if (isTypeIncluded) {
                        selectionBuilder.append(" != ?");
                        selectionArgsList.add("0");
                    } else {
                        DebugUtils.dumpOnMainThread(new IllegalStateException("Should not get here"));
                        selectionBuilder.append(" != ?");
                        selectionArgsList.add("0");
                    }

                    atLeastOneTypeDefined = true;
                }
            }
            if (atLeastOneTypeDefined) {
                selectionBuilder.append(")");
            }
        }

        {
            // Name piece
            final String namePiece = filter.getNamePiece();
            if (namePiece != null && !namePiece.isEmpty()) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.TITLE).append(" LIKE ?");
                selectionArgsList.add("%" + namePiece + "%");
            }
        }

        {
            // Folder path
            final String folderPath = filter.getFolderPath();
            if (folderPath != null && !folderPath.isEmpty()) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.DATA).append(" LIKE ?");
                selectionArgsList.add("%" + folderPath + "/%");
            }
        }

        {
            // Filepath
            final String filepath = filter.getFilepath();
            if (filepath != null && !filepath.isEmpty()) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.DATA).append(" LIKE ?");
                selectionArgsList.add(filepath);
            }
        }

        {
            // Album ID
            final long albumId = filter.getAlbumId();
            if (albumId != SongFilter.ID_NOT_SET) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.ALBUM_ID).append(" = ?");
                selectionArgsList.add(String.valueOf(albumId));
            }
        }

        {
            // Artist ID
            final long artistId = filter.getArtistId();
            if (artistId != SongFilter.ID_NOT_SET) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.ARTIST_ID).append(" = ?");
                selectionArgsList.add(String.valueOf(artistId));
            }
        }

        {
            // Genre ID
            final long genreId = filter.getGenreId();
            if (genreId != SongFilter.ID_NOT_SET) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.GENRE_ID).append(" = ?");
                selectionArgsList.add(String.valueOf(genreId));
            }
        }

        {
            // Min duration
            final long minDuration = filter.getMinDuration();
            if (minDuration != SongFilter.DURATION_NOT_SET && minDuration > 0) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.DURATION).append(" >= ?");
                selectionArgsList.add(String.valueOf(minDuration));
            }
        }

        {
            // Max duration
            final long maxDuration = filter.getMaxDuration();
            if (maxDuration != SongFilter.DURATION_NOT_SET) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.DURATION).append(" <= ?");
                selectionArgsList.add(String.valueOf(maxDuration));
            }
        }

        {
            // Time added
            final long timeAdded = filter.getTimeAdded();
            if (timeAdded != SongFilter.TIME_NOT_SET) {
                if (selectionBuilder.length() > 0) selectionBuilder.append(" AND ");

                selectionBuilder.append(MediaStore.Audio.Media.DATE_ADDED).append(" >= ?");
                selectionArgsList.add(String.valueOf(timeAdded));
            }
        }

        final String selection = selectionBuilder.toString();
        final String[] selectionArgs;
        if (!selectionArgsList.isEmpty()) {
            selectionArgs = new String[selectionArgsList.size()];
            selectionArgsList.toArray(selectionArgs);
        } else {
            selectionArgs = null;
        }

        return new SelectionWithArgs(selection, selectionArgs);
    }

    private static boolean blockingHasEntries(ContentResolver resolver, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = resolver.query(uri, OPT_EMPTY_PROJECTION, selection, selectionArgs, null);
        if (cursor == null) {
            throw new NullPointerException("Query to " + uri + " returned null cursor");
        }
        try {
            return cursor.getCount() > 0;
        } finally {
            cursor.close();
        }
    }

    private static <T> Flowable<List<T>> filterImpl(
            ContentResolver resolver, Flowable<List<T>> source,
            Function<T, Uri> uriFunc, Function<T, SongFilter> filterFunc) {
        return source.switchMap(items -> {
            if (items.isEmpty()) {
                return Flowable.just(items);
            }

            List<Single<List<T>>> filteredSources = new ArrayList<>(items.size());
            for (final T item : items) {
                Single<List<T>> filteredSource = Single.fromCallable(() -> {
                    try {
                        SongFilter resultFilter = filterFunc.apply(item);
                        SongQueryHelper.SelectionWithArgs selectionWithArgs =
                                SongQueryHelper.getSelectionWithArgs(resultFilter);
                        Uri uri = uriFunc.apply(item);
                        if (blockingHasEntries(resolver, uri, selectionWithArgs.selection, selectionWithArgs.args)) {
                            return Collections.singletonList(item);
                        }
                        return Collections.emptyList();
                    } catch (Throwable error) {
                        DebugUtils.dumpOnMainThread(error);
                        return Collections.singletonList(item);
                    }
                });
                filteredSource = filteredSource.subscribeOn(ContentExecutors.workerScheduler());
                filteredSources.add(filteredSource);
            }
            Function<Object[], List<T>> zipper = objects -> {
                List<T> heap = new ArrayList<>();
                for (Object object : objects) {
                    heap.addAll((List<T>) object);
                }
                return heap;
            };
            Single<List<T>> resultSingle = Single.zip(filteredSources, zipper);
            return resultSingle.toFlowable().onBackpressureLatest();
        });
    }

    static Flowable<List<Album>> filterAlbums(
            ContentResolver resolver, Flowable<List<Album>> source, final SongFilter filter) {
        if (filter.getTypes().isEmpty()) {
            return Flowable.just(Collections.emptyList());
        }
        return filterImpl(resolver, source,
                album -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                album -> filter.newBuilder().setAlbumId(album.getId()).build());
    }

    static Flowable<List<Artist>> filterArtists(
            ContentResolver resolver, Flowable<List<Artist>> source, final SongFilter filter) {
        if (filter.getTypes().isEmpty()) {
            return Flowable.just(Collections.emptyList());
        }
        return filterImpl(resolver, source,
                artist -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                artist -> filter.newBuilder().setArtistId(artist.getId()).build());
    }

    static Flowable<List<Genre>> filterGenres(
            ContentResolver resolver, Flowable<List<Genre>> source, final SongFilter filter) {
        if (filter.getTypes().isEmpty()) {
            return Flowable.just(Collections.emptyList());
        }
        return filterImpl(resolver, source,
                genre -> MediaStore.Audio.Genres.Members.getContentUri("external", genre.getId()),
                genre -> filter);
    }

    private SongQueryHelper() {
    }
}
