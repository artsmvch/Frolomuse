package com.frolo.muse.di.impl.local;

import androidx.annotation.NonNull;

import com.frolo.muse.model.media.Song;

import java.util.List;


final class PlaylistCreationOp {
    @NonNull
    final String name;
    @NonNull
    final List<Song> songs;

    PlaylistCreationOp(@NonNull String name, @NonNull List<Song> songs) {
        this.name = name;
        this.songs = songs;
    }
}
