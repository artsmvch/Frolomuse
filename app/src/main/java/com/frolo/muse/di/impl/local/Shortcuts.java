package com.frolo.muse.di.impl.local;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.TypedValue;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.frolo.muse.R;
import com.frolo.muse.ThreadStrictMode;
import com.frolo.muse.broadcast.Broadcasts;
import com.frolo.muse.model.media.Album;
import com.frolo.muse.model.media.Artist;
import com.frolo.muse.model.media.Genre;
import com.frolo.muse.model.media.Media;
import com.frolo.muse.model.media.MediaFile;
import com.frolo.muse.model.media.MyFile;
import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;
import com.frolo.muse.model.media.UnknownMediaException;
import com.frolo.muse.ui.main.MainActivity;
import com.frolo.muse.util.BitmapUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


final class Shortcuts {
    private Shortcuts() {
    }

    private static final String SHARED_PLAYLIST_PREFIX = "playlist_";
    private static final String APP_PLAYLIST_PREFIX = "app_playlist_";

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
                    prefix = SHARED_PLAYLIST_PREFIX;
                } else {
                    // New playlist storage
                    prefix = APP_PLAYLIST_PREFIX;
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
            case Media.SONG:        return ((Song) media).getTitle();
            case Media.ALBUM:       return ((Album) media).getName();
            case Media.ARTIST:      return ((Artist) media).getName();
            case Media.GENRE:       return ((Genre) media).getName();
            case Media.PLAYLIST:    return ((Playlist) media).getName();
            case Media.MY_FILE:     return ((MyFile) media).getJavaFile().getAbsolutePath();
            case Media.MEDIA_FILE: {
                MediaFile mediaFile = (MediaFile) media;
                String name = mediaFile.getName();
                return name != null ? name : "";
            }
            default:                throw new UnknownMediaException(media);
        }
    }

    @WorkerThread
    @NonNull
    private static ShortcutParams createShortcutParams(
            @NonNull Context context, @NonNull Media media) {

        ThreadStrictMode.assertBackground();

        String shortcutId = getShortcutId(media);
        String shortcutLabel = getShortcutLabel(media);
        Intent shortcutIntent = getShortcutIntent(context, media);
        Bitmap icon = tryGetShortcutIcon(context, media);

        return new ShortcutParams(shortcutId, shortcutLabel, shortcutIntent, icon);
    }

    @AnyThread
    @NonNull
    private static ShortcutInfoCompat toShortcutInfoCompat(
            @NonNull Context context, @NonNull ShortcutParams params) {

        final ShortcutInfoCompat.Builder shortcutInfoBuilder =
                new ShortcutInfoCompat.Builder(context, params.shortcutId);

        if (params.icon != null) {
            shortcutInfoBuilder.setIcon(IconCompat.createWithBitmap(params.icon));
        } else {
            shortcutInfoBuilder.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher));
        }

        shortcutInfoBuilder.setShortLabel(params.shortcutLabel);

        shortcutInfoBuilder.setIntent(params.intent);

        return shortcutInfoBuilder.build();
    }

    @MainThread
    private static void requestPinShortcut(
            @NonNull Context context, @NonNull ShortcutParams params) {
        ThreadStrictMode.assertMain();

        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            throw new IllegalStateException("Pin shortcuts are not supported");
        }

        final ShortcutInfoCompat shortcutInfo = toShortcutInfoCompat(context, params);

        // Preparing callback
        Intent callbackIntent = new Intent(Broadcasts.ACTION_SHORTCUT_PINNED);
        callbackIntent.setPackage(context.getPackageName());
        PendingIntent callback = PendingIntent.getBroadcast(
                context, 0, callbackIntent, 0);

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, callback.getIntentSender());
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
            @NonNull final Context context, @NonNull final Media media) throws UnknownMediaException {

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
        return Single.fromCallable(() -> {
            @Media.Kind int kindOfMedia = media.getKind();
            if (kindOfMedia == Media.MY_FILE || kindOfMedia == Media.MEDIA_FILE) {
                // We do not support shortcuts for models with type of MY_FILE and MEDIA_FILE
                return false;
            }

            return ShortcutManagerCompat.isRequestPinShortcutSupported(context);
        });
    }

    static Completable createMediaShortcut(@NonNull final Context context, @NonNull final Media media) {
        return Single.fromCallable(() -> createShortcutParams(context, media))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(shortcutParams -> requestPinShortcut(context, shortcutParams))
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

    static Completable updateMediaShortcut(@NonNull Context context, @NonNull Media media) {
        return Completable.fromAction(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                return;
            }

            ShortcutManager manager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            if (manager == null) {
                return;
            }

            String shortcutId = getShortcutId(media);
            List<ShortcutInfo> pinnedShortcuts = manager.getPinnedShortcuts();
            ShortcutInfo targetShortcut = null;
            for (ShortcutInfo info : pinnedShortcuts) {
                if (info.getId().equals(shortcutId)) {
                    targetShortcut = info;
                    break;
                }
            }

            if (targetShortcut != null) {
                ShortcutParams params = createShortcutParams(context, media);
                ShortcutInfoCompat infoCompat = toShortcutInfoCompat(context, params);
                List<ShortcutInfo> toUpdate = Collections.singletonList(infoCompat.toShortcutInfo());
                boolean wasUpdated = manager.updateShortcuts(toUpdate);
                if (!wasUpdated) {
                    // It's sad
                }
            }
        });
    }

    // Playlist transfer
    static Completable transferPlaylistShortcuts(
            @NonNull final Context context, @NonNull final List<PlaylistTransfer.Result> transfers) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return Completable.fromAction(() -> transferPlaylistShortcuts_API25(context, transfers))
                    .subscribeOn(AndroidSchedulers.mainThread());
        }

        return Completable.complete();
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private static void transferPlaylistShortcuts_API25(
            @NonNull Context context, @NonNull List<PlaylistTransfer.Result> transfers) {
        ShortcutManager manager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
        if (manager == null) {
            return;
        }

        List<ShortcutInfo> pinnedShortcuts = manager.getPinnedShortcuts();

        List<String> legacyPlaylistShortcutIds = new ArrayList<>();
        //List<Playlist> playlistShortcutIntents = new ArrayList<>();
        for (ShortcutInfo info : pinnedShortcuts) {
            String shortcutId = info.getId();
            // Special prefix (see getShortcutId method)
            String idPrefix = SHARED_PLAYLIST_PREFIX;
            if (shortcutId.startsWith(idPrefix)) {
                // It's a legacy playlist shortcut
                String mediaIdString = shortcutId.replaceAll(idPrefix, "");
                try {
                    // If parsed well...
                    long mediaId = Long.parseLong(mediaIdString);
                    legacyPlaylistShortcutIds.add(shortcutId);

                    // Let's find the corresponding target playlist
                    Playlist targetPlaylist = null;
                    for (PlaylistTransfer.Result transfer : transfers) {
                        if (transfer.original.getId() == mediaId) {
                            targetPlaylist = transfer.outcome;
                            break;
                        }
                    }
                    if (targetPlaylist != null && !targetPlaylist.isFromSharedStorage()) {
                        //playlistShortcutIntents.add(targetPlaylist);
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        // NOTE: we should only disable the old shortcuts and not pin new ones,
        // because pinning requires user interaction, which can be confusing for the user,
        // and we just want to transfer playlists unnoticed.
        manager.disableShortcuts(legacyPlaylistShortcutIds);
    }

}
