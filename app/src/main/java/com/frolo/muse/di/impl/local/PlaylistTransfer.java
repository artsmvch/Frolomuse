package com.frolo.muse.di.impl.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.muse.model.media.Playlist;
import com.frolo.muse.model.media.Song;

import java.util.List;


final class PlaylistTransfer {

    final static class Op {
        @NonNull
        final Playlist original;

        @NonNull
        final List<Song> songs;

        Op(@NonNull Playlist original, @NonNull List<Song> songs) {
            this.original = original;
            this.songs = songs;
        }
    }

    final static class Result {
        @NonNull
        final Playlist original;

        @Nullable
        final Playlist outcome;

        Result(@NonNull Playlist original, @Nullable Playlist outcome) {
            this.original = original;
            this.outcome = outcome;
        }
    }
}
