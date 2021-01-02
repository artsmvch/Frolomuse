package com.frolo.muse.di.impl.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.UnknownMediaException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;


final class PlaylistHelper {

    private static final String[] PROJECTION_PLAYLIST_MEMBER =
            new String[] { MediaStore.Audio.Playlists.Members.AUDIO_ID };

    /**
     * All the updates, insertions and deletions in playlists
     * should be performed on {@link WorkerHandler#sInstance} handler.
     * This will prevent unwanted errors when some audio elements are moved or deleted at the same time.
     * {@link WorkerHandler#sInstance} is lazy initialized and thread-safe.
     */
    private static class WorkerHandler {
        private final static Handler sInstance;
        static {
            HandlerThread thread = new HandlerThread("PlaylistHelperWorker");
            thread.start();
            sInstance = new Handler(thread.getLooper());
        }
    }

    /**
     * Wraps <code>action</code> into a completable source.
     * <code>action</code> will be performed on {@link WorkerHandler#sInstance} handler.
     *
     * All the update, insertion and deletion actions should be wrapped with this.
     * This is made to prevent unwanted errors, @see {@link WorkerHandler#sInstance}.
     *
     * @param action to run.
     * @return completable source.
     */
    private static Completable wrap(final Action action) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter emitter) {
                final Runnable command = new Runnable() {
                    @Override
                    public void run() {
                        if (!emitter.isDisposed()) {
                            try {
                                action.run();
                                emitter.onComplete();
                            } catch (Throwable e) {
                                emitter.onError(e);
                            }
                        }
                    }
                };

                emitter.setDisposable(Disposables.fromAction(new Action() {
                    @Override
                    public void run() {
                        WorkerHandler.sInstance.removeCallbacks(command);
                    }
                }));

                if (!emitter.isDisposed()) {
                    WorkerHandler.sInstance.post(command);
                }
            }
        });
    }

    /**
     * Calculates the count of the rows in the cursor returned by the query performed for <code>uri</code>.
     * The query is performed with null selection.
     * @param resolver content resolver.
     * @param uri checked uri.
     * @return the count of the rows.
     * @throws Exception if any
     */
    private static int countRows_Internal(
        ContentResolver resolver,
        Uri uri
    ) throws Exception {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Querying "count(*)" is not supported in Q
            String[] projection = { BaseColumns._ID };
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;
            Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

            if (cursor == null) {
                throw Query.genNullCursorErr(uri);
            }

            final int base;
            try {
                base = cursor.getCount();
            } finally {
                cursor.close();
            }

            return base;
        } else {
            String[] projection = { "count(*)" };
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;
            Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

            if (cursor == null) {
                throw Query.genNullCursorErr(uri);
            }

            final int base;
            try {
                cursor.moveToFirst();
                base = cursor.getInt(0);
            } finally {
                cursor.close();
            }

            return base;
        }
    }

    private static boolean checkPlaylistHasAudioMember_Internal(
        ContentResolver resolver,
        long playlistId,
        long audioId
    ) {
        boolean hasAudioMember = false;

        Uri playlistUri = MediaStore.Audio.Playlists.Members.getContentUri(
                "external", playlistId);
        Cursor cursor = resolver.query(
                playlistUri,
                PROJECTION_PLAYLIST_MEMBER,
                MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + audioId,
                null,
                null);

        if (cursor != null) {
            try {
                hasAudioMember = cursor.moveToFirst();
            } finally {
                cursor.close();
            }
        }

        return hasAudioMember;
    }

    private static void insertAndNotify_Internal(
        ContentResolver resolver,
        Uri uri,
        ContentValues values
    ) {
        // TODO: check the result uri somehow
        Uri resultUri = resolver.insert(uri, values);
        // notify listeners
        resolver.notifyChange(uri, null);
    }

    private static void addAudioToPlaylist_Internal(
        ContentResolver resolver,
        long playlistId,
        long audioId
    ) throws Exception {
        if (checkPlaylistHasAudioMember_Internal(resolver, playlistId, audioId))
            return;

        Uri uri = MediaStore.Audio.Playlists.Members
                .getContentUri("external", playlistId);

        final int base = countRows_Internal(resolver, uri);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + audioId);
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);

        insertAndNotify_Internal(resolver, uri, values);
    }

    private static void addAudioQueryToPlaylist_Internal(
        ContentResolver resolver,
        long playlistId,
        String selection,
        String[] selectionArgs
    ) throws Exception {

        Uri playlistUri = MediaStore.Audio.Playlists.Members
                .getContentUri("external", playlistId);

        final int base = countRows_Internal(resolver, playlistUri);

        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] audioProjection = new String[] { MediaStore.Audio.Media._ID };
        Cursor audioCursor = resolver.query(
                audioUri, audioProjection, selection, selectionArgs, null);

        if (audioCursor == null) {
            throw Query.genNullCursorErr(audioUri);
        }

        try {
            if (audioCursor.moveToFirst()) {
                do {
                    long audioId = audioCursor.getInt(
                            audioCursor.getColumnIndex(audioProjection[0]));

                    if (checkPlaylistHasAudioMember_Internal(resolver, playlistId, audioId)) {
                        continue;
                    }

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + audioId);
                    values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);

                    insertAndNotify_Internal(resolver, playlistUri, values);
                } while (audioCursor.moveToNext());
            }
        } finally {
            audioCursor.close();
        }
    }

    private static void addAlbumToPlaylist_Internal(
        ContentResolver resolver,
        long playlistId,
        long albumId
    ) throws Exception {
        String selection = "is_music != 0 and album_id = " + albumId;
        addAudioQueryToPlaylist_Internal(resolver, playlistId, selection, null);
    }

    private static void addArtistToPlaylist_Internal(
        ContentResolver resolver,
        long playlistId,
        long artistId
    ) throws Exception {
        String selection = "is_music != 0 and artist_id = " + artistId;
        addAudioQueryToPlaylist_Internal(resolver, playlistId, selection, null);
    }

    private static void addMyFileToPlaylist_Internal(
        ContentResolver resolver,
        long playlistId,
        MyFile myFile
    ) throws Exception {
        String selection = MediaStore.Audio.Media.DATA + " like ?";
        // as audio file
        String[] argsAsAudioFile = { "%" + myFile.getJavaFile().getAbsolutePath() };
        addAudioQueryToPlaylist_Internal(resolver, playlistId, selection, argsAsAudioFile);
        // as folder
        String[] argsAsFolder = { "%" + myFile.getJavaFile().getAbsolutePath() + "/%"};
        addAudioQueryToPlaylist_Internal(resolver, playlistId, selection, argsAsFolder);
    }

    private static void addGenreToPlaylist_Internal(
        ContentResolver resolver,
        long playlistId,
        long genreId
    ) throws Exception {

        Uri playlistUri = MediaStore.Audio.Playlists.Members
                .getContentUri("external", playlistId);

        final int base = countRows_Internal(resolver, playlistUri);

        Uri genreUri = MediaStore.Audio.Genres.Members
                .getContentUri("external", genreId);
        String[] genreProjection = new String[] { MediaStore.Audio.Media._ID };
        Cursor genreCursor = resolver
                .query(genreUri, genreProjection, null, null, null);

        if (genreCursor == null) {
            throw Query.genNullCursorErr(playlistUri);
        }

        try {
            if (genreCursor.moveToFirst()) {
                do {
                    long audioId = genreCursor.getInt(
                            genreCursor.getColumnIndex(genreProjection[0]));

                    if (checkPlaylistHasAudioMember_Internal(
                            resolver, playlistId, audioId)) {
                        continue;
                    }

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + audioId);
                    values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);

                    insertAndNotify_Internal(resolver, playlistUri, values);
                } while (genreCursor.moveToNext());
            }
        } finally {
            genreCursor.close();
        }
    }

    /*package*/ static Completable addAlbumToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final long albumId
    ) {
        return Completable.fromAction(new Action() {
            @Override public void run() throws Exception {
                addAlbumToPlaylist_Internal(resolver, playlistId, albumId);
            }
        });
    }

    /*package*/ static Completable addArtistToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final long artistId
    ) {
        return wrap(new Action() {
            @Override public void run() throws Exception {
                addArtistToPlaylist_Internal(resolver, playlistId, artistId);
            }
        });
    }

    /*package*/ static Completable addMyFileToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final MyFile myFile
    ) {
        return wrap(new Action() {
            @Override public void run() throws Exception {
                addMyFileToPlaylist_Internal(resolver, playlistId, myFile);
            }
        });
    }

    /*package*/ static Completable addGenreToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final long genreId
    ) {
        return wrap(new Action() {
            @Override public void run() throws Exception {
                addGenreToPlaylist_Internal(resolver, playlistId, genreId);
            }
        });
    }

    /*package*/ static Completable addSongToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final long audioId
    ) {
        return wrap(new Action() {
            @Override public void run() throws Exception {
                addAudioToPlaylist_Internal(resolver, playlistId, audioId);
            }
        });
    }

    /*package*/ static <E extends Media> Completable addItemToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final E item
    ) {
        return wrap(new Action() {
            @Override public void run() throws Exception {
                switch (item.getKind()) {
                    case Media.SONG: {
                        addAudioToPlaylist_Internal(resolver, playlistId, item.getId());
                        break;
                    }

                    case Media.ALBUM: {
                        addAlbumToPlaylist_Internal(resolver, playlistId, item.getId());
                        break;
                    }

                    case Media.ARTIST: {
                        addArtistToPlaylist_Internal(resolver, playlistId, item.getId());
                        break;
                    }

                    case Media.GENRE: {
                        addGenreToPlaylist_Internal(resolver, playlistId, item.getId());
                        break;
                    }

                    case Media.MY_FILE: {
                        addMyFileToPlaylist_Internal(resolver, playlistId, (MyFile) item);
                        break;
                    }

                    case Media.PLAYLIST: {
                        throw new IllegalArgumentException(
                                "It's not allowed to add a playlist to another playlist"
                        );
                    }
                    default: {
                        throw new UnknownMediaException(item);
                    }
                }
            }
        });
    }

    /*package*/ static <E extends Media> Completable addItemsToPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final Collection<E> items
    ) {
        List<Completable> sources = new ArrayList<>(items.size());
        for (E item : items) {
            sources.add(addItemToPlaylist(resolver, playlistId, item));
        }
        return Completable.merge(sources);
    }

    /*package*/ static Completable removeFromPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final Song song
    ) {
        return wrap(new Action() {
            @Override
            public void run() throws Exception {
                Uri uri = MediaStore.Audio.Playlists.Members.
                        getContentUri("external", playlistId);

                String where = MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + song.getId();
                int deletedCount = resolver.delete(uri, where, null);

                // TODO: check deletedCount
            }
        });
    }

    /*package*/ static Completable moveItemInPlaylist(
        final ContentResolver resolver,
        final long playlistId,
        final int fromPosition,
        final int toPosition
    ) {
        return wrap(new Action() {
            @Override public void run() throws Exception {
                boolean moved = MediaStore.Audio.Playlists.Members
                        .moveItem(resolver, playlistId, fromPosition, toPosition);

                if (!moved) {
                    String msg = "Failed to move item in playlist: "
                            + " from " + fromPosition + " to " + toPosition;
                    throw new Exception(msg);
                }
            }
        });
    }

    private PlaylistHelper() {
    }
}
