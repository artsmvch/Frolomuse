package com.frolo.muse.di.impl.local;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.frolo.muse.R;
import com.frolo.muse.ThreadStrictMode;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.UnknownMediaException;
import com.frolo.muse.ui.main.MainActivity;
import com.frolo.muse.util.BitmapUtil;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


final class Shortcuts {
    private Shortcuts() {
    }

    private static class ShortcutParams {
        @NonNull
        final String shortcutId;

        @NonNull
        final String shortcutLabel;

        @NonNull
        final Intent intent;

        @Nullable
        final Bitmap icon;

        ShortcutParams(@NonNull String shortcutId, @NonNull String shortcutLabel, @NotNull Intent intent, @Nullable Bitmap icon) {
            this.shortcutId = shortcutId;
            this.shortcutLabel = shortcutLabel;
            this.intent = intent;
            this.icon = icon;
        }
    }

    @NonNull
    private static String getShortcutId(@NonNull Media media) {
        final String prefix;
        switch (media.getKind()) {
            case Media.ALBUM:
                prefix = "album_";
                break;
            case Media.ARTIST:
                prefix = "artist_";
                break;
            case Media.GENRE:
                prefix = "genre_";
                break;
            case Media.MY_FILE:
                prefix = "myfile_";
                break;
            case Media.MEDIA_FILE:
                prefix = "mediafile_";
                break;
            case Media.PLAYLIST:
                Playlist playlist = (Playlist) media;
                if (playlist.isFromSharedStorage()) {
                    // Legacy
                    prefix = "playlist_";
                } else {
                    // New playlist storage
                    prefix = "app_playlist_";
                }
                break;
            case Media.SONG:
                prefix = "song_";
                break;
            default:
                prefix = "none_";
                break;
        }

        return prefix + media.getId();
    }

    @NonNull
    private static String getShortcutLabel(@NonNull Media media) {
        switch (media.getKind()) {
            case Media.ALBUM:       return ((Album) media).getName();
            case Media.ARTIST:      return ((Artist) media).getName();
            case Media.GENRE:       return ((Genre) media).getName();
            case Media.MY_FILE:     return ((MyFile) media).getJavaFile().getAbsolutePath();
            case Media.PLAYLIST:    return ((Playlist) media).getName();
            case Media.SONG:        return ((Song) media).getTitle();
            default:                throw new UnknownMediaException(media);
        }
    }

    @MainThread
    private static void installShortcut_Internal(
        @NonNull Context context,
        @NonNull ShortcutParams params
    ) throws Exception {
        ThreadStrictMode.assertMain();

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            throw new IllegalStateException("Pin shortcuts are not supported");
        }

        final ShortcutInfoCompat.Builder shortcutInfoBuilder =
                new ShortcutInfoCompat.Builder(context, params.shortcutId);

        if (params.icon != null) {
            shortcutInfoBuilder.setIcon(IconCompat.createWithBitmap(params.icon));
        } else {
            shortcutInfoBuilder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher));
        }

        shortcutInfoBuilder.setShortLabel(params.shortcutLabel);

        shortcutInfoBuilder.setIntent(params.intent);

        final ShortcutInfoCompat shortcutInfo = shortcutInfoBuilder.build();

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
    }

    @WorkerThread
    @Nullable
    private static Bitmap tryGetShortcutIcon(@NonNull final Context context, @NonNull Media media) {
        if (media.getKind() == Media.SONG) {
            final Song song = (Song) media;
            return tryGetAlbumIcon(context, song.getAlbumId());
        }

        if (media.getKind() == Media.ALBUM) {
            final Album album = (Album) media;
            return tryGetAlbumIcon(context, album.getId());
        }

        return null;
    }

    @WorkerThread
    @Nullable
    private static Bitmap tryGetAlbumIcon(@NonNull final Context context, long albumId) {
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        final Uri albumArtUri = ContentUris.withAppendedId(artworkUri, albumId);

        try {
            final Bitmap original = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), albumArtUri);
            if (original == null) {
                return null;
            }

            // TODO: is 200x200 a good size for it?
            final Bitmap scaled =
                    Bitmap.createScaledBitmap(original, 200, 200, true);

            if (original != scaled) {
                original.recycle();
            }

            final float cornerRadiusInDp = 4f;
            final float cornerRadiusInPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        cornerRadiusInDp,
                        context.getResources().getDisplayMetrics()
                    );
            final Bitmap rounded = BitmapUtil.createRoundedBitmap(scaled, cornerRadiusInPx);

            if (scaled != rounded) {
                scaled.recycle();
            }

            return rounded;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Creates a shortcut intent for the given <code>media</code>.
     * The function returns an intent that opens {@link MainActivity}.
     * @param context context
     * @param media for which to create a shortcut intent
     * @return shortcut intent single source
     */
    @NonNull
    private static Intent getShortcutIntent(
        @NonNull final Context context,
        @NonNull final Media media
    ) throws UnknownMediaException {

        if (media.getKind() == Media.SONG) {
            return MainActivity.newSongIntent(context, (Song) media);
        }

        if (media.getKind() == Media.ALBUM) {
            return MainActivity.newAlbumIntent(context, (Album) media);
        }

        if (media.getKind() == Media.ARTIST) {
            return MainActivity.newArtistIntent(context, (Artist) media);
        }

        if (media.getKind() == Media.GENRE) {
            return MainActivity.newGenreIntent(context, (Genre) media);
        }

        if (media.getKind() == Media.PLAYLIST) {
            return MainActivity.newPlaylistIntent(context, (Playlist) media);
        }

        if (media.getKind() == Media.MY_FILE) {
            return MainActivity.newMyFileIntent(context, (MyFile) media);
        }

        throw new UnknownMediaException(media);
    }

    static Single<Boolean> isShortcutSupported(@NonNull Context context, @NonNull Media media) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                @Media.Kind int kindOfMedia = media.getKind();
                if (kindOfMedia == Media.MY_FILE || kindOfMedia == Media.MEDIA_FILE) {
                    // We do not support shortcuts for models with type of MY_FILE and MEDIA_FILE
                    return false;
                }

                return ShortcutManagerCompat.isRequestPinShortcutSupported(context);
            }
        });
    }

    static Completable createMediaShortcut(@NonNull final Context context, @NonNull final Media media) {
        return Single.fromCallable(new Callable<ShortcutParams>() {
            @Override
            public ShortcutParams call() throws Exception {
                final String shortcutId = getShortcutId(media);
                final String shortcutLabel = getShortcutLabel(media);
                final Intent shortcutIntent = getShortcutIntent(context, media);
                final Bitmap icon = tryGetShortcutIcon(context, media);
                return new ShortcutParams(shortcutId, shortcutLabel, shortcutIntent, icon);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(new Consumer<ShortcutParams>() {
                    @Override
                    public void accept(ShortcutParams shortcutParams) throws Exception {
                        installShortcut_Internal(context, shortcutParams);
                    }
                })
                .ignoreElement();
    }

    static Completable createSongShortcut(@NonNull Context context, @NonNull Song song) {
        return createMediaShortcut(context, song);
    }

    static Completable createAlbumShortcut(@NonNull Context context, @NonNull Album album) {
        return createMediaShortcut(context, album);
    }

    static Completable createArtistShortcut(@NonNull Context context, @NonNull Artist artist) {
        return createMediaShortcut(context, artist);
    }

    static Completable createGenreShortcut(@NonNull Context context, @NonNull Genre genre) {
        return createMediaShortcut(context, genre);
    }

    static Completable createPlaylistShortcut(@NonNull Context context, @NonNull Playlist playlist) {
        return createMediaShortcut(context, playlist);
    }

    static Completable createMyFileShortcut(@NonNull Context context, @NonNull MyFile myFile) {
        return createMediaShortcut(context, myFile);
    }

}
