package com.frolo.muse.model.media;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;


public final class SongComparators {

    public static final Comparator<Song> BY_TRACK_NUMBER = new BaseComparator<Song>() {
        @Override
        public int safelyCompare(@NotNull Song song1, @NotNull Song song2) {
            return song1.getTrackNumber() - song2.getTrackNumber();
        }
    };

    private SongComparators() {
    }
}
