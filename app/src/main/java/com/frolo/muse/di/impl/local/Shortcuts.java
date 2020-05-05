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

    @MainThread
    private static void installShortcut_Internal(
        @NonNull Context context,
        @NonNull Intent shortcutIntent,
        @NonNull Media media,
        @Nullable Bitmap icon
    ) throws Exception {
        ThreadStrictMode.assertMain();

        final String mediaName;
        switch (media.getKind()) {
            case Media.ALBUM:
                mediaName = ((Album) media).getName();
                break;
            case Media.ARTIST:
                mediaName = ((Artist) media).getName();
                break;
            case Media.GENRE:
                mediaName = ((Genre) media).getName();
                break;
            case Media.MY_FILE:
                mediaName = ((MyFile) media).getJavaFile().getAbsolutePath();
                break;
            case Media.PLAYLIST:
                mediaName = ((Playlist) media).getName();
                break;
            case Media.SONG:
                mediaName = ((Song) media).getTitle();
                break;
            default:
                throw new UnknownMediaException(media);
        }

        final Context applicationContext = context.getApplicationContext();

        // Additional setups on the shortcut intent
//        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mediaName);
        if (icon != null) {
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        } else {
            final Intent.ShortcutIconResource iconResource =
                    Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        }
        addIntent.putExtra("duplicate", false);
        applicationContext.sendBroadcast(addIntent);
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

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(), albumArtUri);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                } catch (Exception ignored) {
                }

                return new BitmapResult(bitmap);
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
