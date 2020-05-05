package com.frolo.muse.di.impl.local;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;


final class Shortcuts {
    private Shortcuts() {
    }

    /**
     * Data holder for nullable {@link Bitmap}.
     * Dummy workaround for RxJava2 that does not allow nullable types.
     */
    private static class BitmapResult {
        @Nullable
        final Bitmap bitmap;

        BitmapResult(@Nullable Bitmap bitmap) {
            this.bitmap = bitmap;
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
            case Media.PLAYLIST:
                prefix = "playlist_";
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
        @NonNull Intent shortcutIntent,
        @NonNull Media media,
        @Nullable Bitmap icon
    ) throws Exception {
        ThreadStrictMode.assertMain();

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            throw new IllegalStateException("Pin shortcuts are not supported");
        }

        final String shortcutId = getShortcutId(media);

        final ShortcutInfoCompat.Builder shortcutInfoBuilder =
                new ShortcutInfoCompat.Builder(context, shortcutId);

        if (icon != null) {
            shortcutInfoBuilder.setIcon(IconCompat.createWithBitmap(icon));
        } else {
            shortcutInfoBuilder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher));
        }

        shortcutInfoBuilder.setShortLabel(getShortcutLabel(media));

        shortcutInfoBuilder.setIntent(shortcutIntent);

        final ShortcutInfoCompat shortcutInfo = shortcutInfoBuilder.build();

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
    }

    private static Single<BitmapResult> getIcon(@NonNull final Context context, @NonNull Media media) {
        if (media.getKind() == Media.SONG) {
            return Single.defer(new Callable<SingleSource<? extends BitmapResult>>() {
                @Override
                public SingleSource<? extends BitmapResult> call() throws Exception {
                    final Song song = (Song) media;
                    return getIconForAlbumId(context, song.getAlbumId());
                }
            });
        }

        if (media.getKind() == Media.ALBUM) {
            return Single.defer(new Callable<SingleSource<? extends BitmapResult>>() {
                @Override
                public SingleSource<? extends BitmapResult> call() throws Exception {
                    final Album album = (Album) media;
                    return getIconForAlbumId(context, album.getId());
                }
            });
        }

        return Single.just(new BitmapResult(null));
    }

    private static Single<BitmapResult> getIconForAlbumId(@NonNull final Context context, long albumId) {
        return Single.fromCallable(new Callable<BitmapResult>() {
            @Override
            public BitmapResult call() throws Exception {
                final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
                final Uri albumArtUri = ContentUris.withAppendedId(artworkUri, albumId);

                try {
                    final Bitmap original = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(), albumArtUri);
                    if (original == null) {
                        return new BitmapResult(null);
                    }

                    // TODO: is 200x200 a good size for it?
                    final Bitmap scaled =
                            Bitmap.createScaledBitmap(original, 200, 200, true);

                    if (original != scaled) {
                        original.recycle();
                    }

                    return new BitmapResult(scaled);
                } catch (Exception ignored) {
                    return new BitmapResult(null);
                }
            }
        });
    }

    /**
     * Creates a shortcut intent for the given <code>media</code>.
     * The function returns an intent that opens {@link MainActivity}.
     * @param context context
     * @param media for which to create a shortcut intent
     * @return shortcut intent single source
     */
    private static Single<Intent> createShortcutIntent(@NonNull final Context context, @NonNull final Media media) {
        return Single.fromCallable(new Callable<Intent>() {
            @Override
            public Intent call() throws Exception {
                if (media.getKind() == Media.SONG) {
                    return MainActivity.Companion.newSongIntent(context, (Song) media);
                }

                if (media.getKind() == Media.ALBUM) {
                    return MainActivity.Companion.newAlbumIntent(context, (Album) media);
                }

                if (media.getKind() == Media.ARTIST) {
                    return MainActivity.Companion.newArtistIntent(context, (Artist) media);
                }

                if (media.getKind() == Media.GENRE) {
                    return MainActivity.Companion.newGenreIntent(context, (Genre) media);
                }

                if (media.getKind() == Media.PLAYLIST) {
                    return MainActivity.Companion.newPlaylistIntent(context, (Playlist) media);
                }

                if (media.getKind() == Media.MY_FILE) {
                    return MainActivity.Companion.newMyFileIntent(context, (MyFile) media);
                }

                throw new UnknownMediaException(media);
            }
        });
    }

    static Single<Boolean> isShortcutSupported(@NonNull Context context, @NonNull Media media) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (media.getKind() == Media.MY_FILE) {
                    // We do not support shortcuts for MyFile media models at all
                    return false;
                }

                return ShortcutManagerCompat.isRequestPinShortcutSupported(context);
            }
        });
    }

    static Completable createMediaShortcut(@NonNull final Context context, @NonNull final Media media) {
        return Single.zip(createShortcutIntent(context, media), getIcon(context, media), new BiFunction<Intent, BitmapResult, Object>() {
            @Override
            public Object apply(Intent intent, BitmapResult bitmapResult) throws Exception {
                installShortcut_Internal(context, intent, media, bitmapResult.bitmap);
                return new Object();
            }
        }).ignoreElement().subscribeOn(AndroidSchedulers.mainThread());
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
