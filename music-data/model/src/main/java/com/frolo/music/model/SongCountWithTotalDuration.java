package com.frolo.music.model;

import java.util.Objects;


/**
 * Encapsulates info about a song collection, namely:
 * song count and total duration.
 */
public final class SongCountWithTotalDuration {
    private final int songCount;
    private final int totalDuration;

    public SongCountWithTotalDuration(int songCount, int totalDuration) {
        this.songCount = songCount;
        this.totalDuration = totalDuration;
    }

    public int getSongCount() {
        return songCount;
    }

    /**
     * Returns total duration of songs in milliseconds.
     * @return total duration of songs in milliseconds
     */
    public int getTotalDuration() {
        return totalDuration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof SongCountWithTotalDuration)) return false;

        final SongCountWithTotalDuration other = (SongCountWithTotalDuration) obj;

        return songCount == other.songCount && totalDuration == other.totalDuration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(songCount, totalDuration);
    }

    @Override
    public String toString() {
        return "SongCountWithTotalDuration{" +
                "songCount=" + songCount +
                ", totalDuration=" + totalDuration +
                '}';
    }
}
