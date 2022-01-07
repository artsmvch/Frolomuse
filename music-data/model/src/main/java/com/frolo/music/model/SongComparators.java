package com.frolo.music.model;

import androidx.annotation.NonNull;

import java.util.Comparator;


public final class SongComparators {

    public static final Comparator<Song> BY_TRACK_NUMBER = new BaseComparator<Song>() {
        @Override
        public int safelyCompare(@NonNull Song song1, @NonNull Song song2) {
            return song1.getTrackNumber() - song2.getTrackNumber();
        }
    };

    private SongComparators() {
    }
}
